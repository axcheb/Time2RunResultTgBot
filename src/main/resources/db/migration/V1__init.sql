create table "athlete"
(
    "barcode_id"    integer primary key,
    "name"          text not null,
    "updated_at"    date not null
);

CREATE UNIQUE INDEX IF NOT EXISTS `index_athlete_barcode_id` ON "athlete" ("barcode_id");
