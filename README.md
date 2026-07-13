# FINT Flyt Resource Server

Reaktiv Spring Boot auto-konfigurasjon som standardiserer OAuth2 resource-server-oppførsel for FINT Flyt-tjenester.
Den setter opp WebFlux security filter chains for hvert interne/eksterne API-segment, beriker JWT-er med
Kafka-baserte tillatelser, og holder utledede autoriteter cachet slik at nedstrøms applikasjoner kan beskytte sine
egne HTTP-endepunkter med konsistent semantikk.

## Høydepunkter

- **Reaktive security filter chains** — `SecurityConfiguration` bygger dedikerte WebFlux `SecurityWebFilterChain`-er
  per API-segment (intern admin, intern bruker, intern klient, ekstern) og bruker et delt autorisasjonslogg-filter.
- **Kontroll av policy for flere organisasjoner** — API-segmenter slås av/på via
  `novari.flyt.resource-server.security.api.*`-egenskaper, slik at hver tjeneste kan eksponere kun de flatene som
  trengs for sin organisasjon.
- **Kafka-basert autorisasjon** — `SourceApplicationAuthorizationRequestService` utfører request/reply-oppslag for
  kildeapplikasjons-ID-er, mens `UserPermissionCachingListenerFactory` strømmer brukertillatelser inn i cache.
- **FINT cache-integrasjon** — Langvarige tillatelsesdata lagres i en `FintCache` for å unngå gjentatte
  Kafka-rundturer og for å øke hastigheten på rollesjekker.
- **Klar for observability** — Leveres med Spring Boot Actuator aktivert slik at hver vertstjeneste arver helse- og
  metrikk-probes under `/actuator/**`.

## Arkitekturoversikt

| Komponent                                   | Ansvar                                                                                                              |
|---------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `SecurityConfiguration`                     | Kobler sammen actuator-, admin-, bruker-, intern-klient- og eksterne filter chains med riktige matchere og rekkefølge. |
| `SecurityWebFilterChainFactoryService`      | Bruker tverrgående WebFlux security-konfigurasjon (logging, CSRF av, JWT-konverter-oppkobling, deny/permit-hjelpere). |
| `SourceApplicationJwtConverter`             | Konverterer eksterne klient-JWT-er til autoriteter ved å be om autorisasjonsdetaljer over Kafka.                     |
| `InternalClientJwtConverter`                | Mapper interne klient-subject-claims til `CLIENT_ID_*`-autoriteter brukt av intern-klient filter chain.             |
| `UserJwtConverter`                          | Beriker brukertokener med org-filtrerte roller og cachede kildeapplikasjons-ID-er hentet fra Kafka.                  |
| `UserPermissionCachingListenerFactory`      | Bygger en Kafka-listener som holder `FintCache<UUID, UserPermission>` oppdatert for JWT-konverteren.                 |
| `SourceApplicationAuthorizationRequestService` | Håndterer Kafka request/reply-infrastruktur som løser klient-ID-er til kildeapplikasjons-ID-er.                    |
| `UserAuthorizationService`                  | Verktøy brukt av konsumenter av denne starteren for å verifisere rollemedlemskap eller applikasjonsnivå-tilgang under kjøring. |

## HTTP API

Basestier beskyttet av starteren (se `UrlPaths`):

| Metode | Sti                       | Beskrivelse                                                                                        | Request body                                   | Response                                                                 |
|--------|---------------------------|-----------------------------------------------------------------------------------------------------|------------------------------------------------|--------------------------------------------------------------------------|
| `ANY`  | `/api/intern/admin/**`    | Internt admin-API som krever minst `ADMIN`-rollen; deaktivert med mindre `internal.enabled=true`.   | – (implementert av vertstjenesten)             | Beskyttet nedstrøms-respons, `401/403` når JWT-en mangler autoriteter.   |
| `ANY`  | `/api/intern/**`          | Internt bruker-API som krever `USER`-rollen; filtrerer tillatte roller per organisasjon.             | –                                              | Samme som over.                                                          |
| `ANY`  | `/api/intern-klient/**`   | Internt klient-API sikret av spesifikke klient-ID-er innebygd i JWT-subjektet.                       | –                                              | Samme som over.                                                          |
| `ANY`  | `/api/**`                 | Eksternt API for godkjente kildeapplikasjoner annonsert gjennom Kafka.                               | –                                              | Samme som over.                                                          |

Tokener forventes å inneholde claims nedenfor; starteren ekstraherer disse for å beregne tildelte autoriteter:

```json
{
  "organizationid": "vtfk.no",
  "objectidentifier": "7d9e0d20-2a50-4ca1-9e4f-7c79f5fbe6c0",
  "roles": [
    "https://role-catalog.vigoiks.no/vigo/flyt/user"
  ],
  "sub": "client-123" // interne/eksterne klienter bruker subjektet som clientId
}
```

Feil faller tilbake til Spring Security-standarder: manglende/ugyldige tokener resulterer i `401 Unauthorized`, mens
nektede autoriteter svarer med `403 Forbidden`.

## Kafka-integrasjon

- `SourceApplicationAuthorizationRequestService` oppretter autorisasjonsforespørsel-topics med standard
  org/applikasjons-prefiks, setter opp kortlevde reply-topics (2 minutters lagringstid), og bruker `RequestTemplate`
  til å utføre request/reply-kall som oversetter klient-ID-er til kildeapplikasjons-ID-er.
- `UserPermissionCachingListenerFactory` lytter på `userpermission`-entitetstopicen ved bruk av
  `ParameterizedListenerContainerFactoryService`, skriver poster inn i den delte `FintCache`, og hopper over
  feilede poster via en `ErrorHandlerFactory`.
- Kafka-tilkobling, gruppe-ID-er og polling-parametere baserer seg på de delte `no.novari:kafka`-hjelperne slik at
  starteren arver FINT-standardverdier (max poll-innstillinger, seek-to-beginning-oppstart, osv.).

## Planlagte oppgaver

Ingen planlagte jobber er definert; tillatelsers levetid håndteres utelukkende gjennom Kafka-lyttere og den
konfigurerte cache-TTL-en (`novari.cache.default-cache-entry-time-to-live`) slik at ingen cron-lignende opprydding er
nødvendig.

## Konfigurasjon

Sentrale egenskaper eksponert av starteren:

| Egenskap                                                              | Beskrivelse                                                                                       |
|-------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `novari.flyt.resource-server.security.api.internal.enabled`             | Aktiverer interne admin/bruker-API-er og registrerer Kafka-baserte brukerautorisasjons-beans.       |
| `novari.flyt.resource-server.security.api.internal.authorized-org-id-role-pairs-json` | JSON-map av `{ "orgId": ["USER","ADMIN"] }` som filtrerer tillatte roller per organisasjon.       |
| `novari.flyt.resource-server.security.api.internal-client.enabled`      | Slår på filter chain for internt klient-API.                                                        |
| `novari.flyt.resource-server.security.api.internal-client.authorized-client-ids` | Liste over JWT-subjekter som kan kalle `/api/intern-klient/**`.                                  |
| `novari.flyt.resource-server.security.api.external.enabled`             | Slår på filter chain for eksternt API.                                                              |
| `novari.flyt.resource-server.security.api.external.authorized-source-application-ids` | Liste over kildeapplikasjons-ID-er autorisert for `/api/**`.                                     |
| `novari.kafka.application-id`                                           | Brukes til navngiving av request/reply-topics og listener-gruppe-ID-er.                             |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri`                  | Utsteder for JWT-validering (`https://idp.felleskomponent.no/nidp/oauth/nam`).                      |
| `novari.cache.default-cache-entry-time-to-live`                         | Standard cache-TTL (10 år som standard) for cachede tillatelsesposter.                              |

Hemmeligheter levert via den konsumerende tjenestens deployment (Kubernetes secret, Vault, osv.) må gi OAuth-klientdata,
Kafka-tilkoblingsopplysninger, og eventuelle organisasjonsspesifikke JSON-payloads nevnt over.

## Kjøre lokalt

Forutsetninger:

- Java 25+
- Gradle (wrapper inkludert)
- Kafka-megler (for å kjøre tillatelseslytteren/request-reply-interaksjoner)

Nyttige kommandoer:

```shell
./gradlew clean build         # kompiler og kjør hele testsuiten
./gradlew test                # kun enhetstester
./gradlew publishToMavenLocal # installer starteren lokalt slik at andre apper kan avhenge av den under utvikling
```

For å eksperimentere med starteren inne i en annen Spring Boot-tjeneste, legg til `no.novari:fint-flyt-resource-server`
som en avhengighet, kjør en lokal Kafka-megler (f.eks. docker-compose), og aktiver ønsket API-segment:

```shell
SPRING_APPLICATION_JSON='{
  "novari":{
    "flyt":{
      "resource-server":{
        "security":{
          "api":{
            "internal":{
              "enabled":true,
              "authorized-org-id-role-pairs-json":"{\"vtfk.no\":[\"USER\",\"ADMIN\"]}"
            }
          }
        }
      }
    },
    "kafka":{
      "application-id":"fint-flyt-resource-server-local"
    }
  }
}' ./gradlew bootRun
```

## Utrulling

- Artefakter publiseres til `https://repo.fintlabs.no/releases` via `./gradlew publish`, som gjenbruker
  Gradles publishing credentials (`REPOSILITE_USERNAME/PASSWORD`).
- Konsumenter henter starteren som en Maven-avhengighet; Spring Boot oppdager automatisk auto-konfigurasjonene
  gjennom `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- Koordiner utrullinger med nedstrøms tjenester slik at de kan oppgradere avhengighetsversjonen og tilpasse
  Kafka/OAuth-innstillinger.

## Sikkerhet

- Konfigurerer Spring Securitys WebFlux resource server med utsteder-validering mot `https://idp.felleskomponent.no`.
- Sti-nivå-segmentering sikrer at `/api/intern*`-endepunkter forblir interne mens `/api/**` er begrenset til
  autoriserte kildeapplikasjoner utledet fra Kafka.
- `AuthorityMappingService` og `AuthorityPrefix` standardiserer hvordan autoriteter kodes og senere parses av
  konsumenttjenester.
- `AuthorizationLogFilter` sporer Authorization-headere (på TRACE-nivå) for å hjelpe med å diagnostisere
  token-problemer uten å lekke dem på høyere loggnivåer.
- Intern rolleevaluering filtrerer token-roller per organisasjon og utvider underforståtte roller
  (Developer → Admin → User).

## Observability og drift

- Helse- og readiness-probes arves gjennom Spring Boot Actuator (`/actuator/health`, `/actuator/readiness`,
  `/actuator/prometheus` når aktivert av vertsappen).
- Cache-tilstand kan inspiseres via `FintCacheManager`-metrikker når Micrometer er aktivert i den konsumerende
  tjenesten.
- Kafka-lyttere logger konsumerte tillatelseshendelser på DEBUG-nivå slik at operatører kan spore
  autorisasjonsendringer ved behov.

## Utviklingstips

- Når `authorized-org-id-role-pairs-json` oppdateres, sørg for at payloaden er gyldig JSON; starteren logger
  parse-feil ved oppstart.
- Bruk `UserAuthorizationService` inne i vertstjenester for å beskytte handlere på samme måte som filter chains gjør.
- Integrasjonstester kan mocke `ReplyTopicService` og `RequestTemplateFactory` for å unngå Kafka samtidig som
  JWT-konverterne fortsatt dekkes.
- `UserPermissionCachingListenerFactory` gjenbruker standard Kafka-innstillinger; overstyr
  listener-konfigurasjonen kun hvis du virkelig trenger andre poll-størrelser eller retry-logikk.

## Bidra

1. Opprett en topic-branch.
2. Kjør `./gradlew test` (eller `./gradlew clean build`) før du åpner en PR.
3. Koordiner versjonsoppgraderinger av avhengigheter med konsumenter og dokumenter eventuelle nye
   konfigurasjonsnøkler.
4. Legg til eller juster enhetstester ved endringer i konvertere, mapping-tjenester eller Kafka-lyttere.

———

FINT Flyt Resource Server vedlikeholdes av FINT Flyt-teamet. Ta kontakt på den interne Slack-kanalen eller opprett en
sak i dette repositoriet for spørsmål eller forbedringer.
