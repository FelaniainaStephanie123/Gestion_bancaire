--
-- PostgreSQL database dump
--

\restrict zDdqieCeBx3MFTt5YU3FzOi0cpE6dyqPT0geyNr8ljAhsElxl9M4j0JHRkGPbNl

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: fn_apres_virement(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_apres_virement() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE client SET solde_actuel = solde_actuel - NEW.montant
        WHERE num_compte = NEW.num_compte_envoyeur;
    UPDATE client SET solde_actuel = solde_actuel + NEW.montant
        WHERE num_compte = NEW.num_compte_beneficiaire;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.fn_apres_virement() OWNER TO postgres;

--
-- Name: fn_avant_virement(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_avant_virement() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    solde_dispo NUMERIC(15,2);
BEGIN
    SELECT solde_actuel INTO solde_dispo
      FROM client WHERE num_compte = NEW.num_compte_envoyeur;

    IF solde_dispo IS NULL THEN
        RAISE EXCEPTION 'Compte envoyeur introuvable';
    ELSIF solde_dispo < NEW.montant THEN
        RAISE EXCEPTION 'Solde insuffisant pour effectuer ce virement';
    END IF;

    RETURN NEW;
END;
$$;


ALTER FUNCTION public.fn_avant_virement() OWNER TO postgres;

--
-- Name: fn_maj_solde_sur_modification_pret(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_maj_solde_sur_modification_pret() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF (TG_OP = 'UPDATE') THEN
        -- On retire l'ancien montant et on ajoute le nouveau montant au solde du client
        UPDATE client 
        SET solde_actuel = solde_actuel - OLD.montant_prete + NEW.montant_prete
        WHERE num_compte = NEW.num_compte;
        RETURN NEW;
        
    ELSIF (TG_OP = 'DELETE') THEN
        -- Si on supprime le prˆt, on retire son montant du solde du client
        UPDATE client 
        SET solde_actuel = solde_actuel - OLD.montant_prete
        WHERE num_compte = OLD.num_compte;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$;


ALTER FUNCTION public.fn_maj_solde_sur_modification_pret() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: agent; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.agent (
    id_agent integer NOT NULL,
    nom_utilisateur character varying(50) NOT NULL,
    mot_de_passe character varying(255) NOT NULL,
    nom_complet character varying(100),
    date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    role character varying(20) DEFAULT 'GUICHETIER'::character varying
);


ALTER TABLE public.agent OWNER TO postgres;

--
-- Name: agent_id_agent_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.agent_id_agent_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.agent_id_agent_seq OWNER TO postgres;

--
-- Name: agent_id_agent_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.agent_id_agent_seq OWNED BY public.agent.id_agent;


--
-- Name: client; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client (
    num_compte character varying(20) NOT NULL,
    nom character varying(100) NOT NULL,
    prenoms character varying(150),
    tel character varying(20),
    mail character varying(150),
    solde_actuel numeric(15,2) DEFAULT 0 NOT NULL,
    date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.client OWNER TO postgres;

--
-- Name: notification_email; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notification_email (
    id bigint NOT NULL,
    destinataire character varying(255) NOT NULL,
    sujet character varying(255) NOT NULL,
    contenu text NOT NULL,
    date_envoi timestamp without time zone NOT NULL,
    envoyee boolean DEFAULT false NOT NULL,
    envoyee_le timestamp without time zone
);


ALTER TABLE public.notification_email OWNER TO postgres;

--
-- Name: notification_email_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notification_email_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notification_email_id_seq OWNER TO postgres;

--
-- Name: notification_email_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notification_email_id_seq OWNED BY public.notification_email.id;


--
-- Name: preter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.preter (
    num_pret character varying(20) NOT NULL,
    num_compte character varying(20) NOT NULL,
    montant_prete numeric(15,2) NOT NULL,
    taux_interet numeric(5,2) DEFAULT 10.00 NOT NULL,
    montant_a_rendre numeric(15,2) GENERATED ALWAYS AS ((montant_prete * ((1)::numeric + (taux_interet / (100)::numeric)))) STORED,
    date_pret date NOT NULL,
    date_echeance date,
    CONSTRAINT preter_montant_prete_check CHECK ((montant_prete > (0)::numeric))
);


ALTER TABLE public.preter OWNER TO postgres;

--
-- Name: rendre; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rendre (
    num_rendu character varying(20) NOT NULL,
    num_pret character varying(20) NOT NULL,
    situation character varying(20) NOT NULL,
    montant_paye numeric(15,2) NOT NULL,
    date_rendu date DEFAULT CURRENT_DATE NOT NULL,
    CONSTRAINT rendre_montant_paye_check CHECK ((montant_paye > (0)::numeric)),
    CONSTRAINT rendre_situation_check CHECK (((situation)::text = ANY (ARRAY[('TOTAL'::character varying)::text, ('PARTIEL'::character varying)::text])))
);


ALTER TABLE public.rendre OWNER TO postgres;

--
-- Name: v_benefice_banque; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_benefice_banque AS
 SELECT count(*) AS nombre_prets,
    sum(montant_prete) AS total_prete,
    sum(((montant_prete * taux_interet) / (100)::numeric)) AS benefice_total
   FROM public.preter;


ALTER VIEW public.v_benefice_banque OWNER TO postgres;

--
-- Name: virement; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.virement (
    num_virement character varying(20) NOT NULL,
    num_compte_envoyeur character varying(20) NOT NULL,
    num_compte_beneficiaire character varying(20) NOT NULL,
    montant numeric(15,2) NOT NULL,
    date_transfert timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_virement_comptes_differents CHECK (((num_compte_envoyeur)::text <> (num_compte_beneficiaire)::text)),
    CONSTRAINT virement_montant_check CHECK ((montant > (0)::numeric))
);


ALTER TABLE public.virement OWNER TO postgres;

--
-- Name: v_detail_virement; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_detail_virement AS
 SELECT v.num_virement,
    v.date_transfert,
    v.montant,
    ce.num_compte AS num_compte_envoyeur,
    ce.nom AS nom_envoyeur,
    ce.prenoms AS prenoms_envoyeur,
    ce.solde_actuel AS solde_envoyeur,
    cb.num_compte AS num_compte_beneficiaire,
    cb.nom AS nom_beneficiaire,
    cb.prenoms AS prenoms_beneficiaire
   FROM ((public.virement v
     JOIN public.client ce ON (((ce.num_compte)::text = (v.num_compte_envoyeur)::text)))
     JOIN public.client cb ON (((cb.num_compte)::text = (v.num_compte_beneficiaire)::text)));


ALTER VIEW public.v_detail_virement OWNER TO postgres;

--
-- Name: v_situation_prets; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_situation_prets AS
SELECT
    NULL::character varying(20) AS num_pret,
    NULL::character varying(20) AS num_compte,
    NULL::character varying(100) AS nom,
    NULL::character varying(150) AS prenoms,
    NULL::numeric(15,2) AS montant_prete,
    NULL::numeric(15,2) AS montant_a_rendre,
    NULL::numeric AS total_paye,
    NULL::numeric AS reste_a_payer,
    NULL::date AS date_pret,
    NULL::date AS date_echeance,
    NULL::text AS situation_actuelle;


ALTER VIEW public.v_situation_prets OWNER TO postgres;

--
-- Name: agent id_agent; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.agent ALTER COLUMN id_agent SET DEFAULT nextval('public.agent_id_agent_seq'::regclass);


--
-- Name: notification_email id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification_email ALTER COLUMN id SET DEFAULT nextval('public.notification_email_id_seq'::regclass);


--
-- Data for Name: agent; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.agent (id_agent, nom_utilisateur, mot_de_passe, nom_complet, date_creation, role) FROM stdin;
1	admin	240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9	Administrateur	2026-08-16 20:35:40.560964	GUICHETIER
2	administrateur	240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9	Superviseur Principal	2026-08-19 08:20:33.889977	ADMIN
\.


--
-- Data for Name: client; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client (num_compte, nom, prenoms, tel, mail, solde_actuel, date_creation) FROM stdin;
200543	RAKOTO	Bernard	0341234567	rakoto.bernard@mail.com	14811000.00	2026-08-11 22:12:39.092196
202908	RANDRIA	Barthelemy	0339876543	randria.b@mail.com	480889.00	2026-08-11 22:12:39.092196
202910	Bob	alias	0342837230	ranro@gmail.com	1000.00	2026-08-18 08:38:59.727859
CLI002	yuta	okkotsu	0380192749	a@gmail.com	1000.00	2026-08-18 09:58:28.105638
202909	Jean	Grey	0380000000	example1@gmail.com	5500.00	2026-08-16 11:10:43.14668
CLI003	Maki	Zenin	0380000000	a@gmail.com	2450.00	2026-08-18 10:14:00.696023
CLI004	alias	alias	0380192730	alais1.sage463@slmails.com	4900.00	2026-08-18 20:40:09.164574
CLI005	gram	mod	0382038192	b@gmail.com	650.00	2026-08-18 20:52:57.633377
CLI006	NAINA	Stephanie	0334578945	stephanie@gmail.com	8000.00	2026-08-19 10:04:40.031712
CLI007	VAO	Lolo	0324578941	lolo@gmail.com	6000.00	2026-08-19 10:23:08.298047
CLI001	Minaj	Nick	0380000000	n@gmail.com	3450.00	2026-08-18 09:57:57.502124
CLI008	mob	psy	0384561234	alais1.sage463@slmails.com	2000.00	2026-08-19 11:29:28.76388
\.


--
-- Data for Name: notification_email; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notification_email (id, destinataire, sujet, contenu, date_envoi, envoyee, envoyee_le) FROM stdin;
3	lolo@gmail.com	Rappel de remboursement - pret n°P16	Bonjour VAO Lolo,\n\nRappel : le remboursement de votre pret n°P16 est attendu au plus tard le 2026-09-03.\n\nMerci de regulariser votre situation.\n\nCordialement,\nLa Banque.	2026-09-03 10:23:41.488198	f	\N
5	n@gmail.com	Rappel de remboursement - pret n°P17	Bonjour Minaj Nick,\n\nLe remboursement de votre pret n°P17 est attendu au plus tard le 2026-09-03.\n\nMerci de regulariser votre situation.\n\nCordialement,\nLa Banque.	2026-09-03 11:06:11.485819	f	\N
1	stephanie@gmail.com	Rappel de remboursement - pret n°P15	Bonjour NAINA Stephanie,\n\nRappel : le remboursement de votre pret n°P15 est attendu au plus tard le 2026-09-03.\n\nMerci de regulariser votre situation.\n\nCordialement,\nLa Banque.	2026-08-19 07:25:36.731422	t	2026-08-19 11:23:47.326526
2	lolo@gmail.com	Confirmation de votre pret n°P16	Bonjour VAO Lolo,\n\nVotre pret numero P16 a bien ete accorde.\nLa date limite de remboursement est le 2026-09-03.\n\nMontant prete : 2000 Ar\n\nCordialement,\nLa Banque.	2026-08-19 08:05:27.949844	t	2026-08-19 11:23:51.563247
4	n@gmail.com	Confirmation de votre pret n°P17	Bonjour Minaj Nick,\n\nVotre pret numero P17 a bien ete accorde.\nLa date limite de remboursement est le 2026-09-03.\n\nMontant prete : 1000 Ar\n\nCordialement,\nLa Banque.	2026-08-19 11:06:21.41224	t	2026-08-19 11:23:56.611675
7	alais1.sage463@slmails.com	Rappel de remboursement - pret n°P18	Bonjour mob psy,\n\nLe remboursement de votre pret n°P18 est attendu au plus tard le 2026-09-03.\n\nMerci de regulariser votre situation.\n\nCordialement,\nLa Banque.	2026-09-03 11:30:26.87587	f	\N
6	alais1.sage463@slmails.com	Confirmation de votre pret n°P18	Bonjour mob psy,\n\nVotre pret numero P18 a bien ete accorde.\nLa date limite de remboursement est le 2026-09-03.\n\nMontant prete : 1000 Ar\n\nCordialement,\nLa Banque.	2026-08-19 11:30:36.810414	t	2026-08-19 11:30:47.780852
\.


--
-- Data for Name: preter; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.preter (num_pret, num_compte, montant_prete, taux_interet, date_pret, date_echeance) FROM stdin;
P01	202909	1000.00	10.00	2026-08-17	2026-09-17
P03	202908	2000.00	10.00	2026-08-17	2026-09-17
P04	202908	500.00	10.00	2026-08-18	2026-09-18
P05	202910	1000.00	10.00	2026-08-18	2026-09-18
P06	CLI002	1000.00	10.00	2026-08-18	2026-09-18
P07	CLI003	1000.00	10.00	2026-08-18	2026-09-18
P08	CLI001	1000.00	10.00	2026-08-18	2026-09-18
P09	CLI001	500.00	10.00	2026-08-18	2026-09-18
P10	CLI003	500.00	10.00	2026-08-18	2026-09-18
P11	CLI004	1000.00	10.00	2026-08-18	2026-09-18
P12	CLI004	2000.00	10.00	2026-08-18	2026-09-18
P13	CLI005	500.00	10.00	2026-08-18	2026-09-18
P14	CLI005	200.00	10.00	2026-08-18	2026-09-18
P15	CLI006	3000.00	10.00	2026-08-19	2026-09-19
P16	CLI007	2000.00	10.00	2026-08-19	2026-09-19
P17	CLI001	1000.00	10.00	2026-08-19	2026-09-19
P18	CLI008	1000.00	10.00	2026-08-19	2026-09-19
\.


--
-- Data for Name: rendre; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rendre (num_rendu, num_pret, situation, montant_paye, date_rendu) FROM stdin;
R001	P01	PARTIEL	1000.00	2026-08-17
R002	P03	TOTAL	2200.00	2026-08-17
R003	P05	PARTIEL	1000.00	2026-08-18
R004	P06	PARTIEL	1000.00	2026-08-18
R005	P06	TOTAL	100.00	2026-08-18
R006	P04	PARTIEL	500.00	2026-08-18
R007	P04	TOTAL	50.00	2026-08-18
R008	P07	TOTAL	1100.00	2026-08-18
R009	P05	TOTAL	100.00	2026-08-18
R010	P01	TOTAL	100.00	2026-08-18
R011	P08	PARTIEL	500.00	2026-08-18
R012	P08	TOTAL	600.00	2026-08-18
R013	P09	PARTIEL	500.00	2026-08-18
R014	P09	TOTAL	50.00	2026-08-18
R015	P10	TOTAL	550.00	2026-08-18
R016	P11	TOTAL	1100.00	2026-08-18
R017	P13	TOTAL	550.00	2026-08-18
\.


--
-- Data for Name: virement; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.virement (num_virement, num_compte_envoyeur, num_compte_beneficiaire, montant, date_transfert) FROM stdin;
V001	200543	202908	100000.00	2026-08-16 09:45:35.383482
V002	200543	202908	90000.00	2026-08-16 09:56:16.850379
V003	200543	202908	2000.00	2026-08-16 11:16:11.784686
V004	200543	202909	2000.00	2026-08-16 11:16:58.8788
V005	202908	202909	500.00	2026-08-17 08:18:13.163043
V006	202908	202909	2000.00	2026-08-17 22:20:18.267739
\.


--
-- Name: agent_id_agent_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.agent_id_agent_seq', 2, true);


--
-- Name: notification_email_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notification_email_id_seq', 7, true);


--
-- Name: agent agent_nom_utilisateur_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.agent
    ADD CONSTRAINT agent_nom_utilisateur_key UNIQUE (nom_utilisateur);


--
-- Name: agent agent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.agent
    ADD CONSTRAINT agent_pkey PRIMARY KEY (id_agent);


--
-- Name: client client_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT client_pkey PRIMARY KEY (num_compte);


--
-- Name: notification_email notification_email_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification_email
    ADD CONSTRAINT notification_email_pkey PRIMARY KEY (id);


--
-- Name: preter preter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.preter
    ADD CONSTRAINT preter_pkey PRIMARY KEY (num_pret);


--
-- Name: rendre rendre_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rendre
    ADD CONSTRAINT rendre_pkey PRIMARY KEY (num_rendu);


--
-- Name: virement virement_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.virement
    ADD CONSTRAINT virement_pkey PRIMARY KEY (num_virement);


--
-- Name: idx_notification_email_a_traiter; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notification_email_a_traiter ON public.notification_email USING btree (envoyee, date_envoi);


--
-- Name: v_situation_prets _RETURN; Type: RULE; Schema: public; Owner: postgres
--

CREATE OR REPLACE VIEW public.v_situation_prets AS
 SELECT p.num_pret,
    p.num_compte,
    c.nom,
    c.prenoms,
    p.montant_prete,
    p.montant_a_rendre,
    COALESCE(sum(r.montant_paye), (0)::numeric) AS total_paye,
    (p.montant_a_rendre - COALESCE(sum(r.montant_paye), (0)::numeric)) AS reste_a_payer,
    p.date_pret,
    p.date_echeance,
        CASE
            WHEN (COALESCE(sum(r.montant_paye), (0)::numeric) = (0)::numeric) THEN 'Non rembourse'::text
            WHEN (COALESCE(sum(r.montant_paye), (0)::numeric) >= p.montant_a_rendre) THEN 'Tout paye'::text
            ELSE 'Paye une part'::text
        END AS situation_actuelle
   FROM ((public.preter p
     JOIN public.client c ON (((c.num_compte)::text = (p.num_compte)::text)))
     LEFT JOIN public.rendre r ON (((r.num_pret)::text = (p.num_pret)::text)))
  GROUP BY p.num_pret, c.num_compte, c.nom, c.prenoms;


--
-- Name: virement trg_apres_virement; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_apres_virement AFTER INSERT ON public.virement FOR EACH ROW EXECUTE FUNCTION public.fn_apres_virement();


--
-- Name: virement trg_avant_virement; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_avant_virement BEFORE INSERT ON public.virement FOR EACH ROW EXECUTE FUNCTION public.fn_avant_virement();


--
-- Name: preter trg_maj_solde_modif_preter; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_maj_solde_modif_preter AFTER UPDATE ON public.preter FOR EACH ROW EXECUTE FUNCTION public.fn_maj_solde_sur_modification_pret();


--
-- Name: preter trg_maj_solde_suppr_preter; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_maj_solde_suppr_preter AFTER DELETE ON public.preter FOR EACH ROW EXECUTE FUNCTION public.fn_maj_solde_sur_modification_pret();


--
-- Name: preter preter_num_compte_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.preter
    ADD CONSTRAINT preter_num_compte_fkey FOREIGN KEY (num_compte) REFERENCES public.client(num_compte);


--
-- Name: rendre rendre_num_pret_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rendre
    ADD CONSTRAINT rendre_num_pret_fkey FOREIGN KEY (num_pret) REFERENCES public.preter(num_pret);


--
-- Name: virement virement_num_compte_beneficiaire_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.virement
    ADD CONSTRAINT virement_num_compte_beneficiaire_fkey FOREIGN KEY (num_compte_beneficiaire) REFERENCES public.client(num_compte);


--
-- Name: virement virement_num_compte_envoyeur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.virement
    ADD CONSTRAINT virement_num_compte_envoyeur_fkey FOREIGN KEY (num_compte_envoyeur) REFERENCES public.client(num_compte);


--
-- PostgreSQL database dump complete
--

\unrestrict zDdqieCeBx3MFTt5YU3FzOi0cpE6dyqPT0geyNr8ljAhsElxl9M4j0JHRkGPbNl

