#!/bin/bash
# Ortak şube gruplarını uçtan uca dener.
# Kullanım (TimeTables klasöründe, backend açıkken):  bash docs/ortak_test.sh
# Her adımda [GEÇTİ] ya da [HATA] yazar. Sonunda test verileri silinir.

U=http://localhost:8080
ZY=/tmp/ortak-zeynep.txt
CN=/tmp/ortak-can.txt
AD=/tmp/ortak-admin.txt
HATA=0

giris() { curl -s -c "$3" -X POST "$U/api/giris" -H "Content-Type: application/json" -d "{\"eposta\":\"$1\",\"sifre\":\"$2\"}" > /dev/null; }
gonder() { # çerez yöntem adres [gövde]
  if [ -n "$4" ]; then curl -s -b "$1" -X "$2" "$U$3" -H "Content-Type: application/json" -d "$4"; else curl -s -b "$1" -X "$2" "$U$3" -H "Content-Type: application/json"; fi
}
kod() { # çerez yöntem adres [gövde] -> HTTP kodu
  if [ -n "$4" ]; then curl -s -o /dev/null -w '%{http_code}' -b "$1" -X "$2" "$U$3" -H "Content-Type: application/json" -d "$4"; else curl -s -o /dev/null -w '%{http_code}' -b "$1" -X "$2" "$U$3" -H "Content-Type: application/json"; fi
}
p() { python3 -c "import sys,json; d=json.load(sys.stdin); print($1)"; }
kontrol() { # açıklama bulunan beklenen
  if [ "$2" == "$3" ]; then echo "  [GEÇTİ] $1"; else echo "  [HATA] $1 -> bulunan: $2, beklenen: $3"; HATA=$((HATA+1)); fi
}

echo "== Hazırlık"
giris zeynep@ornek.edu.tr Asistan12345 $ZY
giris can@ornek.edu.tr Endustri12345 $CN
giris admin@ornek.edu.tr Admin12345 $AD

PA=$(gonder $ZY POST /api/program-projeleri '{"kurumId":1,"bolumId":1,"ad":"ORTAK TEST A","donem":"2099 Ortak Test"}' | p "d.get('id')")
PB=$(gonder $CN POST /api/program-projeleri '{"kurumId":1,"bolumId":2,"ad":"ORTAK TEST B","donem":"2099 ortak  test"}' | p "d.get('id')")
HA=$(gonder $ZY POST /api/ogretim-elemanlari '{"bolumId":1,"adSoyad":"ORTAK TEST HOCASI"}' | p "d.get('id')")
HB=$(gonder $CN POST /api/ogretim-elemanlari '{"bolumId":2,"adSoyad":"Ortak Test Hocası"}' | p "d.get('id')")
DR1=$(gonder $AD POST /api/derslikler '{"kurumId":1,"ad":"ORTAK-TEST-BUYUK","kapasite":200,"tur":"SINIF"}' | p "d.get('id')")
DR2=$(gonder $AD POST /api/derslikler '{"kurumId":1,"ad":"ORTAK-TEST-KUCUK","kapasite":100,"tur":"SINIF"}' | p "d.get('id')")
DA=$(gonder $ZY POST /api/dersler "{\"projeId\":$PA,\"bolumId\":1,\"kod\":\"ORT-A1\",\"ad\":\"Ortak Test Dersi\",\"tur\":\"NORMAL\",\"haftalikSaat\":2}" | p "d.get('id')")
DB=$(gonder $CN POST /api/dersler "{\"projeId\":$PB,\"bolumId\":2,\"kod\":\"ORT-B1\",\"ad\":\"ORTAK TEST DERSİ\",\"tur\":\"NORMAL\",\"haftalikSaat\":2}" | p "d.get('id')")
SA=$(gonder $ZY POST /api/subeler "{\"dersId\":$DA,\"ogretimElemaniId\":$HA,\"tahminiOgrenci\":60}" | p "d.get('id')")
SB=$(gonder $CN POST /api/subeler "{\"dersId\":$DB,\"ogretimElemaniId\":$HB,\"tahminiOgrenci\":50}" | p "d.get('id')")
BA=$(gonder $ZY GET "/api/bloklar?dersId=$DA" | p "d[0].get('id')")
BB=$(gonder $CN GET "/api/bloklar?dersId=$DB" | p "d[0].get('id')")
echo "  projeler $PA/$PB, hocalar $HA/$HB, derslikler $DR1/$DR2, şubeler $SA/$SB, bloklar $BA/$BB"

echo "== 1. Aday bulma (ad ve hoca aynı, kodlar farklı, dönem yazımı farklı)"
kontrol "Zeynep 1 aday görür" "$(gonder $ZY GET "/api/ortak-gruplar/adaylar?projeId=$PA" | p "len(d)")" "1"
kontrol "Eşleşen şube Can'ın şubesi" "$(gonder $ZY GET "/api/ortak-gruplar/adaylar?projeId=$PA" | p "d[0]['eslesenler'][0]['subeId']==$SB")" "True"
kontrol "Blok yapıları uyumlu" "$(gonder $ZY GET "/api/ortak-gruplar/adaylar?projeId=$PA" | p "d[0]['eslesenler'][0]['bloklarUyumlu']")" "True"

echo "== 2. Grup önerisi ve onay"
kontrol "Tek şubeli istek reddedilir" "$(kod $ZY POST /api/ortak-gruplar "{\"subeIdler\":[$SA]}")" "400"
GRUP=$(gonder $ZY POST /api/ortak-gruplar "{\"subeIdler\":[$SA,$SB]}")
GID=$(echo "$GRUP" | p "d.get('id')")
kontrol "Öneren taraf onaylı, diğer bekliyor" "$(echo "$GRUP" | p "[u.get('onayli') for u in d.get('uyeler', [])]")" "[True, False]"
kontrol "Toplam öğrenci 110" "$(echo "$GRUP" | p "d.get('toplamOgrenci')")" "110"
kontrol "Öneren, kendi onayını beklemiyor" "$(echo "$GRUP" | p "d.get('onayimiBekliyor')")" "False"
kontrol "Can onayını bekleyen grubu görür" "$(gonder $CN GET "/api/ortak-gruplar?projeId=$PB" | p "d[0].get('onayimiBekliyor')")" "True"
kontrol "Zeynep başkasının şubesini onaylayamaz" "$(kod $ZY POST /api/ortak-gruplar/$GID/onayla)" "400"
kontrol "Can onaylar" "$(gonder $CN POST /api/ortak-gruplar/$GID/onayla | p "d.get('tamOnayli')")" "True"
kontrol "İkinci onay denemesi reddedilir" "$(kod $CN POST /api/ortak-gruplar/$GID/onayla)" "400"

echo "== 3. Ortak şubeler aynı yerde olmalı"
kontrol "Zeynep büyük derslikte Pzt 1. saate yerleştirir" "$(kod $ZY PUT /api/bloklar/$BA/yerlesim "{\"gun\":1,\"baslangicSlot\":1,\"derslikIdler\":[$DR1]}")" "200"
kontrol "Can için yalnızca aynı yer uygun (hoca ve derslik çakışması yok sayılır)" "$(gonder $CN GET "/api/bloklar/$BB/uygun-yerler?derslikIdler=$DR1&durum=UYGUN" | p "[(y.get('gun'), y.get('baslangicSlot')) for y in d]")" "[(1, 1)]"
kontrol "Aynı yer bilgi mesajı verir" "$(gonder $CN GET "/api/bloklar/$BB/uygun-yerler?derslikIdler=$DR1&durum=UYGUN" | p "[i.get('kod') for i in (d[0].get('ihlaller', []) if len(d)>0 else [])]")" "['ORTAK_SUBE_AYNI_YERDE']"
kontrol "Can aynı yere yerleştirir" "$(kod $CN PUT /api/bloklar/$BB/yerlesim "{\"gun\":1,\"baslangicSlot\":1,\"derslikIdler\":[$DR1]}")" "200"
kontrol "Can başka güne taşıyamaz" "$(gonder $CN POST /api/bloklar/$BB/yerlesim/kontrol "{\"gun\":2,\"baslangicSlot\":1,\"derslikIdler\":[$DR1]}" | p "[i.get('kod') for i in d.get('ihlaller', []) if i.get('tur')=='ENGEL']")" "['ORTAK_SUBE_FARKLI_YERDE']"
echo "     Mesaj: $(gonder $CN POST /api/bloklar/$BB/yerlesim/kontrol "{\"gun\":2,\"baslangicSlot\":1,\"derslikIdler\":[$DR1]}" | p "next((i.get('mesaj') for i in d.get('ihlaller', [])), '')")"

echo "== 4. Kapasite ortak şubelerin toplamıyla denetlenir"
kontrol "60+50=110 kişi 100 kişilik derslikte uyarı verir" "$(gonder $CN POST /api/bloklar/$BB/yerlesim/kontrol "{\"gun\":1,\"baslangicSlot\":1,\"derslikIdler\":[$DR2]}" | p "'KAPASITE_ASIMI' in [i.get('kod') for i in d.get('ihlaller', [])]")" "True"
echo "     Mesaj: $(gonder $CN POST /api/bloklar/$BB/yerlesim/kontrol "{\"gun\":1,\"baslangicSlot\":1,\"derslikIdler\":[$DR2]}" | p "next((i.get('mesaj') for i in d.get('ihlaller', []) if i.get('kod')=='KAPASITE_ASIMI'), '')")"

echo "== 5. Gruptan ayrılınca ortak kural kalkar, hoca çakışması devreye girer"
kontrol "Can gruptan ayrılır" "$(kod $CN DELETE /api/ortak-gruplar/$GID/uyeler/$SB)" "204"
kontrol "Grup silindi (2'den az üye kaldı)" "$(gonder $ZY GET "/api/ortak-gruplar?projeId=$PA" | p "len(d)")" "0"
kontrol "Aynı hoca başka bölümde aynı saatte engellenir" "$(gonder $CN POST /api/bloklar/$BB/yerlesim/kontrol "{\"gun\":1,\"baslangicSlot\":1,\"derslikIdler\":[$DR2]}" | p "'HOCA_BASKA_BOLUMDE_DERSTE' in [i.get('kod') for i in d.get('ihlaller', [])]")" "True"

echo "== 6. Farklı ders ortak olamaz"
DB2=$(gonder $CN POST /api/dersler "{\"projeId\":$PB,\"bolumId\":2,\"kod\":\"ORT-B2\",\"ad\":\"Baska Ders\",\"tur\":\"NORMAL\",\"haftalikSaat\":2}" | p "d.get('id')")
SB2=$(gonder $CN POST /api/subeler "{\"dersId\":$DB2,\"ogretimElemaniId\":$HB,\"tahminiOgrenci\":30}" | p "d.get('id')")
kontrol "Farklı kod ve ad reddedilir" "$(kod $ZY POST /api/ortak-gruplar "{\"subeIdler\":[$SA,$SB2]}")" "400"
echo "     Mesaj: $(gonder $ZY POST /api/ortak-gruplar "{\"subeIdler\":[$SA,$SB2]}" | p "d.get('mesaj', '')")"

echo "== Temizlik"
gonder $ZY DELETE /api/program-projeleri/$PA > /dev/null
gonder $CN DELETE /api/program-projeleri/$PB > /dev/null
gonder $ZY DELETE /api/ogretim-elemanlari/$HA > /dev/null
gonder $CN DELETE /api/ogretim-elemanlari/$HB > /dev/null
gonder $AD DELETE /api/derslikler/$DR1 > /dev/null
gonder $AD DELETE /api/derslikler/$DR2 > /dev/null
echo "  test verileri silindi"

if [ $HATA -eq 0 ]; then echo "TÜM KONTROLLER GEÇTİ"; else echo "$HATA KONTROL BAŞARISIZ"; fi