# operativni-sistemi-projekat

## 1. Uvod

Ovaj projekat predstavlja simulaciju operativnog sistema napisanu u Javi.
Sistem omogućava kreiranje i izvršavanje procesa, upravljanje memorijom,
rad sa fajl sistemom kroz komandni interfejs (Shell), kao i komunikaciju
sa ulazno-izlaznim uređajima kao što su disk i konzola.

## 2. Komponente sistema

### 2.1 Upravljanje procesima
- **Shell** – komandni interfejs za interakciju sa sistemom
- **CPU** – izvršava instrukcije (LOAD, STORE, ADD, SYSCALL, HALT)
- **Asembler** – prevodi jednoadresni ASM kod u mašinski kod
- **Scheduler (FCFS)** – raspoređuje procese po principu First Come First Served
- **PCB** – struktura koja čuva informacije o svakom procesu

### 2.2 Upravljanje memorijom
- **MemoryManager** – upravlja alokacijom i oslobađanjem memorijskih segmenata
- **Buddy sistem** – dinamička alokacija memorije dijeljenjem i spajanjem blokova
- **RAM** – simulacija fizičke memorije

### 2.3 Fajl sistem
- **Stablo struktura** – organizacija fajlova i direktorijuma
- **Vektor bitova** – praćenje slobodnih i zauzetih blokova na disku

### 2.4 Ulazno-izlazni podsistem
- **DiskDevice (SCAN algoritam)** – simulacija kretanja glave diska
- **DMAController** – direktan prenos podataka između HDD i RAM
- **IOManager** – upravljanje ulazno-izlaznim uređajima
- **ConsoleDevice** – simulacija konzolnog uređaja


## 3. Opis rada komponenti

CPU izvršava instrukcije jednu po jednu iz memorije. Brojač (PC) prati
gdje se trenutno nalazi u programu i poslije svake instrukcije pomjera
se na sljedeću.

Scheduler koristi FCFS princip — proces koji prvi dođe, prvi dobije CPU.
Izvršava se dok ne završi ili dok ne čeka na ulaz/izlaz, tek onda
dolazi sljedeći.

Memorija se upravlja Buddy sistemom — blokovi se dijele na pola kad
treba alocirati, a spajaju se nazad kad se oslobode.

Fajlovi su organizovani u stablo struktura. Vektor bitova prati koji
su blokovi na disku slobodni, a koji zauzeti.

Disk koristi SCAN algoritam — glava ide u jednom smjeru i opslužuje
zahtjeve redom, pa se vraća nazad.

DMA prenosi podatke između diska i memorije direktno, bez opterećivanja
CPU-a, koji u tom vremenu može raditi nešto drugo.

## 4. Pokretanje sistema

Pri pokretanju sistema čita se datoteka `memorija.txt` koja sadrži
inicijalnu strukturu fajl sistema i sistemske procese.
Format datoteke:
DIR /putanja
FILE /putanja/ime.asm ASM_KOD
PROCESS /putanja/ime.asm

## 5. Shell komande

ls, dir - prikaz sadržaja direktorijuma
cd - promjena direktorijuma  
mkdir - kreiranje direktorijuma
touch - kreiranje fajla
cat - prikaz sadržaja fajla
open - otvaranje fajla
write - upis ASM koda
run - pokretanje procesa
ps - prikaz procesa
kill - gašenje procesa
mem - stanje memorije
speed - brzina CPU-a
rm - brisanje
exit - gašenje sistema

## 6. Scenariji izvršavanja

### Scenario 1 – Kreiranje i pokretanje programa

U prvom scenariju demonstrira se osnovni tok rada sa sistemom.
Prikazuje se sadržaj postojećeg fajla, kreira se novi direktorijum i fajl,
upisuje se asemblerski kod koji se prevodi u binarni zapis i čuva na disk,
te se pokreće proces i prikazuje tabela procesa.

cat /Sistem/idle.asm         # 1. prikaži sadržaj
mkdir noviDir                # 2. kreiraj direktorijum
cd /noviDir                  # 3. uđi u njega
touch program.asm            # 4. kreiraj fajl
open program.asm             # 5. otvori (DMA Disk->RAM)
write LOAD 5\nADD 3\nHALT    # 6. upiši ASM kod
run /noviDir/program.asm     # 7. pokreni proces
ps                           # 8. prikaži procese

### Scenario 2 – Stres test sistema

Drugi scenario testira sistem pod opterećenjem. Pokreće se više procesa
istovremeno, prati se njihovo izvršavanje kroz tabelu procesa,
demonstrira se nasilno gašenje procesa komandom kill,
te kreiranje i brisanje fajla uz praćenje vektora bitova.

speed 800                   # uspori CPU da se vidi rad
run /korisnik/projekat.asm  # pokreni proces 1
run /korisnik/projekat.asm  # pokreni proces 2
run /korisnik/projekat.asm  # pokreni proces 3
ps                          # prikaži sve aktivne procese
kill <pid>                  # ubij proces <pid>
ps
mem                         # prikaži stanje memorije
touch /korisnik/test.asm    # kreiraj fajl
rm /korisnik/test.asm       # obrisi fajl