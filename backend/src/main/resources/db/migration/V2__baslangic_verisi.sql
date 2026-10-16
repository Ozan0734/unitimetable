INSERT INTO kurum (ad) VALUES ('Örnek Üniversite');

INSERT INTO zaman_izgarasi (kurum_id)
SELECT id FROM kurum WHERE ad = 'Örnek Üniversite';
