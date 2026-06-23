# operativni-sistemi-projekat

## 1. Uvod

Ovaj projekat predstavlja simulaciju operativnog sistema napisanu u Javi.
Sistem omogućava kreiranje i izvršavanje procesa, upravljanje memorijom,
rad sa fajl sistemom kroz grafički interfejs (GUI) i komandni interfejs (Shell),
kao i komunikaciju sa ulazno-izlaznim uređajima kao što su disk i konzola.
Kernel radi paralelno sa korisničkim interfejsom na zasebnom threadu.

## 2. Komponente sistema

### 2.1 Upravljanje procesima
- **GUI / Shell** – grafički i komandni interfejs za interakciju sa sistemom
- **CPU** – izvršava jednoadresne instrukcije (LOAD, STORE, ADD, MUL, DIV, JMP, JZ, SYSCALL, HALT...)
- **Asembler** – prevodi ASM kod u mašinski kod (opcode + operand)
- **Scheduler (FCFS)** – raspoređuje procese, vremenski kvant 5 instrukcija
- **PCB** – struktura koja čuva informacije o svakom procesu (PID, stanje, PC, registri)

### 2.2 Upravljanje memorijom
- **MemoryManager** – upravlja alokacijom i oslobađanjem memorijskih segmenata
- **Buddy sistem** – dinamička alokacija memorije dijeljenjem i spajanjem blokova
- **RAM** – simulacija fizičke memorije od 1024B
- **Zaštita adresnog prostora** – proces ne može pristupiti memoriji drugog procesa

### 2.3 Fajl sistem
- **Stablo struktura** – Directory i File nasljeđuju FSNode, navigacija kroz resolve()
- **Vektor bitova** – praćenje slobodnih i zauzetih blokova na disku

### 2.4 Ulazno-izlazni podsistem
- **DiskDevice (SCAN algoritam)** – simulacija kretanja glave diska
- **DMAController** – direktan prenos podataka između HDD i RAM na posebnom threadu
- **IOManager** – upravljanje ulazno-izlaznim uređajima, red čekanja za zauzete uređaje
- **ConsoleDevice** – simulacija konzolnog uređaja

## 3. Opis rada komponenti

CPU izvršava instrukcije jednu po jednu iz memorije. Brojač (PC) prati
gdje se trenutno nalazi u programu i poslije svake instrukcije pomjera
se na sljedeću za 2 (opcode + operand).

Scheduler koristi FCFS princip — proces koji prvi dođe, prvi dobije CPU.
Svaki proces dobija kvant od 5 instrukcija, pa se vraća u red čekanja.

Memorija se upravlja Buddy sistemom — blokovi se dijele na pola kad
treba alocirati, a spajaju se nazad kad se oslobode. Zaštita adresnog
prostora sprječava pristup memoriji drugog procesa.

Fajlovi su organizovani u stablo strukturu. Vektor bitova prati koji
su blokovi na disku slobodni, a koji zauzeti.

Disk koristi SCAN algoritam — glava ide u jednom smjeru i opslužuje
zahtjeve redom, pa se vraća nazad.

DMA prenosi podatke između diska i memorije direktno, bez opterećivanja
CPU-a, koji u tom vremenu može raditi nešto drugo.

## 4. Pokretanje sistema

### GUI verzija
Pokrenuti `GUI/FinaliTest.java`. Pri pokretanju se čita `memorija.txt`
koja sadrži inicijalnu strukturu fajl sistema i sistemske procese.

### Shell verzija
Pokrenuti `FS/FinaliTest.java`. Inicijalizacija se vrši kroz
`initSistem()` metodu bez beskonačnih procesa.

Format datoteke `memorija.txt`:
```
DIR /putanja
FILE /putanja/ime.asm ASM_KOD
PROCESS /putanja/ime.asm
```

## 5. Shell komande

Shell podržava sljedeće komande za interakciju sa sistemom:

**Navigacija i fajl sistem**
- `ls` / `dir` — prikazuje fajlove i direktorijume u trenutnom direktorijumu
- `cd <dir>` — prelazi u zadani direktorijum (`cd ..` za povratak)
- `mkdir <ime>` — kreira novi direktorijum
- `touch <ime>` — kreira novi prazan fajl
- `cat <putanja>` — ispisuje sadržaj fajla
- `rm <putanja>` — briše fajl ili direktorijum

**Rad sa fajlovima (DMA)**
- `open <ime>` — otvara fajl za pisanje, pokreće DMA prenos Disk → RAM
- `write <kod>` — upisuje ASM kod u otvoreni fajl i prevodi ga u binarni zapis
- `close` — zatvara fajl i pokreće DMA prenos RAM → Disk

**Procesi i sistem**
- `run <putanja>` — pokreće proces iz fajla, odmah vraća kontrolu korisniku
- `ps` — prikazuje aktivne i završene procese sa svim informacijama
- `kill <pid>` — nasilno gasi proces i oslobađa memoriju
- `mem` — prikazuje trenutno stanje RAM memorije
- `show` — detaljan pregled sistema (memorija, redovi, hardver)
- `speed <ms>` — postavlja brzinu CPU-a (trajanje jednog takta u ms)
- `exit` — gasi sistem

## 6. Scenariji izvršavanja

### Scenario 1 – Kreiranje i pokretanje programa (GUI)

U prvom scenariju demonstrira se osnovni tok rada sa sistemom kroz grafički interfejs.
Prikazuje se sadržaj postojećeg fajla, kreira se novi direktorijum i fajl,
upisuje se asemblerski kod koji se prevodi u binarni zapis i čuva na disk,
te se pokreće proces i prikazuje tabela procesa u Task Manageru.

**Šta radim:**
1. Uđem u direktorijum `/Data` i otvorim postojeći fajl → konzola pokazuje `[DMA] HDD → RAM`
2. Kreiram novi direktorijum klikom na *Novi Direktorijum*
3. Dvostrukim klikom uđem u kreirani direktorijum
4. Kreiram novi fajl klikom na *+ Novi fajl*, naziv npr. `program.asm`
5. Selektujem fajl → kliknem *Otvori fajl* → konzola pokazuje `[DMA] HDD → RAM`
6. U editoru upisujem ASM kod, kliknem *Prevedi* → prikazuje se binarni zapis
7. Kliknem *Sačuvaj* → konzola pokazuje `[DMA] RAM → HDD`
8. Zatvorim editor, selektujem fajl → kliknem *Pokreni fajl*
9. Otvorim *Task Manager* → prikazujem procese i stanje memorije

### Scenario 2 – Stres test sistema (Shell)

Drugi scenario testira sistem pod opterećenjem. Pokreće se više procesa
istovremeno, prati se njihovo izvršavanje kroz tabelu procesa,
demonstrira se nasilno gašenje procesa komandom kill,
te kreiranje i brisanje fajla uz praćenje vektora bitova.

```
speed 800                        # uspori CPU da se vidi rad schedulera
run /User/beskonacni1.asm        # pokreni proces 1
run /User/beskonacni2.asm        # pokreni proces 2
run /User/syscall_test.asm       # pokreni proces koji ulazi u WAITING
ps                               # prikaži sve procese - RUNNING/READY/WAITING
kill <pid>                       # ubij proces
ps                               # provjeri da je uklonjen
mem                              # prikaži stanje Buddy memorije
touch /korisnik/test.asm         # kreiraj fajl - SCAN algoritam vidljiv u konzoli
rm /korisnik/test.asm            # obriši fajl - oslobađanje BitVector bloka
```
Napomena: Isti scenario može se izvesti i kroz GUI — procesi se pokreću
klikom na *Pokreni fajl*, prate se u Task Manageru, a kill se vrši kroz
polje *Kill PID*. U GUI verziji brzina CPU-a nije parametar pa procesi
teku normalnom brzinom.