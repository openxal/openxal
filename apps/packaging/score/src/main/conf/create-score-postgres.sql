
CREATE SCHEMA score AUTHORIZATION score;

CREATE TABLE score.score_snapshot_grp (
    equip_cat_id character varying NOT NULL,
    descr character varying,
    pri_save_set_ind character(1),
    userid character varying,
    mod_dte timestamp without time zone NOT NULL,
	PRIMARY KEY (equip_cat_id, mod_dte)
);
ALTER TABLE score.score_snapshot_grp OWNER TO score;

CREATE TABLE score.score_snapshot_grp_sgnl (
    equip_cat_id character varying NOT NULL,
    set_pt_sgnl_id character varying NOT NULL,
    rb_sgnl_id character varying NOT NULL,
    sys_id character varying(45),
    filter_id character varying(45),
    use_rb_ind character(1),
    active_ind character(1),
    pv_data_type character varying,
	PRIMARY KEY (equip_cat_id, set_pt_sgnl_id, rb_sgnl_id)
);
ALTER TABLE score.score_snapshot_grp_sgnl OWNER TO score;

CREATE TABLE score.score_snapshot_sgnl (
    equip_cat_id character varying NOT NULL,
    mod_dte timestamp without time zone NOT NULL,
    set_pt_sgnl_id character varying NOT NULL,
    rb_sgnl_id character varying NOT NULL,
    rb_sgnl_val character varying,
    set_pt_sgnl_val character varying,
	PRIMARY KEY (equip_cat_id, mod_dte, set_pt_sgnl_id, rb_sgnl_id),
	FOREIGN KEY (equip_cat_id, mod_dte) REFERENCES score.score_snapshot_grp(equip_cat_id, mod_dte),
	FOREIGN KEY (equip_cat_id, set_pt_sgnl_id, rb_sgnl_id) REFERENCES score.score_snapshot_grp_sgnl(equip_cat_id, set_pt_sgnl_id, rb_sgnl_id)
);
ALTER TABLE score.score_snapshot_sgnl OWNER TO score;

