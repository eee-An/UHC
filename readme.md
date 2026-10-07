# Floxy UHC

Minecraft UHC plugin za Paper/Spigot servere, napravljen za organizaciju custom
Ultra Hardcore partija s kontroliranim borderom, supply dropovima, zabranjenim
itemima, golden apple limitom i ceremonijom pobjednika.

## Features

- automatski teleport igrača na konfigurirane spawn lokacije
- konfigurabilan početni world border i težina svijeta
- zakazane akcije tijekom partije:
  - pomicanje i smanjivanje bordera
  - spawnanje supply dropova
- supply dropovi s WorldEdit schematicom, loot tableom, beaconom i compass boss barom
- konfigurabilan limit enchanted golden appleova
- uklanjanje zabranjenih itema pri pickupu
- border upozorenja prije pokretanja pomicanja
- winner ceremony s teleportacijom gledatelja i konfigurabilnim vatrometom
- reload konfiguracije bez restartanja servera
- PlaceholderAPI podrška

## Requirements

- Minecraft/Paper ili Spigot server `26.2` ili noviji
- Java 21 ili novija
- [WorldEdit](https://enginehub.org/worldedit/)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) —
  opcionalno
- [Multiverse-Core](https://www.spigotmc.org/resources/multiverse-core.390/) —
  opcionalno

WorldEdit je obavezan dependency. PlaceholderAPI i Multiverse-Core su soft
dependencies i plugin može raditi bez njih.

### PlaceholderAPI

Plugin registrira expansion `uhc`. Placeholderi se koriste u scoreboardu ili
drugim PlaceholderAPI podržanim pluginima u ovom formatu:

```text
%uhc_line1%
%uhc_razmak%
%uhc_stoperica%
%uhc_line2%
%uhc_line3%
%uhc_line4%
%uhc_line5%
%uhc_line6%
%uhc_line7%
%uhc_line8%
%uhc_line9%
```

`%uhc_stoperica%` prikazuje vrijeme partije, a `%uhc_line3%` prikazuje stanje
sljedećeg ili aktivnog supply dropa. `%uhc_line10%` koristi animaciju
`%animation:pruga%`, pa je za nju potreban plugin koji pruža tu animaciju.

## Installation

1. Preuzmi izgrađeni plugin `.jar`.
2. Instaliraj WorldEdit na server.
3. Opcionalno instaliraj PlaceholderAPI i Multiverse-Core.
4. Kopiraj `UHC.jar` u serverov `plugins` direktorij.
5. Pokreni server jednom kako bi se generirali `config.yml` i schematic datoteke.
6. Uredi `plugins/UHC/config.yml`.
7. Provjeri da world iz `world-name` postoji i ponovno pokreni server.

Schematic datoteke `balon.schem` i `balon2.schem` automatski se kopiraju u
pluginov data direktorij. Supply drop koristi schematic naveden u
`supply-drop-schematic`.

## Commands

Sve komande su prema trenutnoj konfiguraciji dostupne operatorima.

| Command | Description |
| --- | --- |
| `/uhcstart` | Pokreće UHC partiju |
| `/uhcend` | Završava trenutnu partiju |
| `/resetstate` | Vraća stanje plugina u `WAITING` |
| `/bacisupplydrop` | Ručno baca supply drop na lokaciju igrača |
| `/configreload` | Ponovno učitava `config.yml` |
| `/testborder` | Testira pomicanje bordera |

## Configuration

Glavna konfiguracija nalazi se u `src/main/resources/config.yml` i kopira se u
`plugins/UHC/config.yml` pri prvom pokretanju.

Najvažnije sekcije:

```yaml
game:
  difficulty: HARD
  initial-border:
    center-x: 0.5
    center-z: -0.5
    size: 4000.0

start-settings:
  clear-inventory: true
  clear-ender-chest: true
  spawn-slowness:
    enabled: true
    duration: 10
    amplifier: 10

supply-drop:
  glowing: true
  compass-bar-enabled: true
  beacon-enabled: true
```

Zakazane iteme, spawn lokacije, poruke, border warnings, winner ceremony i
vremenski raspored akcija također se konfiguriraju u istom fajlu.

### Scheduled actions

Akcije se izvršavaju prema vremenu od početka partije:

```yaml
scheduled-actions:
  - time: "0:00:10"
    action: "border"
    params: {X: 0.0, Z: 0.0, size: 100.0, delay: 10, duration: 30}
  - time: "0:00:30"
    action: "supplydrop"
    params: {X: 10.0, Y: 165.0, Z: 10.0}
```

Vrijednosti `delay` i `duration` za border izražene su u sekundama, a
`time` koristi format `hours:minutes:seconds`.

## Credits

- **eee-An** — autor projekta
- **IamMusavaRibica** — povezao je cijeli plugin i omogućio da projekt radi kao cjelina
- **GitHub Copilot** — pomoć pri razvoju i dokumentaciji

## License

Ovaj projekt je objavljen pod
[PolyForm Noncommercial 1.0.0 licencom](LICENSE).

Licenca dopušta osobno, edukacijsko i drugo nekomercijalno
korištenje, izmjene i distribuciju uz zadržavanje licence.
Nije dopuštena prodaja plugina, prodaja izvedenih verzija niti
korištenje projekta za stvaranje komercijalne koristi bez
posebnog dopuštenja autora.
