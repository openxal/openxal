---------------  EXECUTE WITH pvlog/pvlog USER -------------------------

-- Default schema used by pvlogger
CREATE SCHEMA pvlog AUTHORIZATION pvlog;
SET search_path = pvlog, pg_catalog;

-- SnapshotGroup
-- Describes the types of snapshots taken. Needs to be manually populated.
CREATE TABLE pvlog.mach_snapshot_type (
    snapshot_type_nm text NOT NULL PRIMARY KEY,
    snapshot_type_desc text,
    snapshot_per double precision,      -- snapshot period in seconds
    snapshot_retent double precision,   -- retention period in days (0 = forever)
    svc_id text                         -- set service environment variable "serviceID" to this value (PHYSICS,TESTFAC,ES&H currently used)
);
GRANT ALL ON TABLE pvlog.mach_snapshot_type TO pvlog;
--ALTER TABLE pvlog.mach_snapshot_type OWNER TO pvlog;

-- SnapshotGroupChannel
-- Lists all channels that are taken by certain snapshot group / type
CREATE TABLE pvlog.mach_snapshot_type_sgnl (
    snapshot_type_nm text NOT NULL REFERENCES pvlog.mach_snapshot_type(snapshot_type_nm),
    sgnl_id text NOT NULL,
    PRIMARY KEY (snapshot_type_nm, sgnl_id)
);
GRANT ALL ON TABLE pvlog.mach_snapshot_type_sgnl TO pvlog;
--ALTER TABLE pvlog.mach_snapshot_type_sgnl OWNER TO pvlog;

-- MachineSnapshot
-- Description and timepoint of actual snapshot
CREATE SEQUENCE pvlog.mach_snapshot_id_seq;
GRANT ALL ON SEQUENCE pvlog.mach_snapshot_id_seq TO pvlog;
CREATE TABLE pvlog.mach_snapshot (
    snapshot_id integer PRIMARY KEY DEFAULT nextval('mach_snapshot_id_seq'::regclass),
    snapshot_dte timestamp without time zone,
    snapshot_type_nm text REFERENCES pvlog.mach_snapshot_type(snapshot_type_nm),
    cmnt text
);
GRANT ALL ON TABLE pvlog.mach_snapshot TO pvlog;
--ALTER TABLE  pvlog.mach_snapshot OWNER TO pvlog;

-- ChannelSnapshot
-- Values of the channels in snapshots
CREATE TABLE pvlog.mach_snapshot_sgnl (
    snapshot_id integer NOT NULL REFERENCES pvlog.mach_snapshot(snapshot_id),
    sgnl_id text NOT NULL,
    sgnl_timestp time without time zone,
    sgnl_val double precision[],
    sgnl_stat integer,
    sgnl_svrty integer,
	PRIMARY KEY (snapshot_id, sgnl_id)
);
GRANT ALL ON TABLE pvlog.mach_snapshot_sgnl TO pvlog;
--ALTER TABLE pvlog.mach_snapshot_sgnl OWNER TO pvlog;


-- Populate snapshot types

INSERT INTO mach_snapshot_type VALUES ('default', 'default snapshot', 30000000000, 0, 'DEFAULT');
INSERT INTO mach_snapshot_type VALUES ('test_physics_snapshot', 'test snapshot description', 60, 0, 'PHYSICS');
INSERT INTO mach_snapshot_type VALUES ('test_testfac_snapshot', 'test snapshot description', 60, 0, 'TESTFAC');
INSERT INTO mach_snapshot_type VALUES ('test_esh_snapshot', 'test snapshot description', 60, 0, 'ES&H');
