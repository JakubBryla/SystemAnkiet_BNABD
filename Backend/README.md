# SystemAnkiet - Backend

Backend aplikacji webowej do przeprowadzania ankiet zbudowany w technologii **Java 21 + Spring Boot 3.4**.

---

## Technologie

| Technologia | Wersja | Rola |
|-------------|--------|------|
| Java | 21 | Jezyk programowania |
| Spring Boot | 3.4.1 | Framework backendowy |
| Spring Security | 6.x | Autentykacja i autoryzacja |
| Spring Data JPA + Hibernate | - | Warstwa dostepu do danych |
| Microsoft SQL Server | - | Baza danych |
| JJWT | 0.12.6 | Tokeny JWT |
| Lombok | - | Redukcja kodu szablonowego |
| Maven | 3.x | Zarzadzanie projektem |

---

## Struktura projektu

```
Backend/
├── src/
│   └── main/
│       ├── java/com/systemankiet/
│       │   ├── SystemAnkietApplication.java   <- punkt wejscia
│       │   ├── config/
│       │   │   └── SecurityConfig.java        <- konfiguracja Spring Security + CORS
│       │   ├── controller/
│       │   │   ├── AuthController.java        <- endpointy logowania i rejestracji
│       │   │   └── SurveyController.java      <- endpointy ankiet
│       │   ├── dto/
│       │   │   ├── LoginRequest.java
│       │   │   ├── RegisterRequest.java
│       │   │   ├── AuthResponse.java
│       │   │   ├── SurveyDto.java
│       │   │   └── CreateSurveyRequest.java
│       │   ├── entity/
│       │   │   ├── User.java                  <- encja uzytkownika (implementuje UserDetails)
│       │   │   └── Survey.java                <- encja ankiety
│       │   ├── enums/
│       │   │   ├── Role.java                  <- USER, ADMIN
│       │   │   └── SurveyStatus.java          <- SZKIC, AKTYWNA, ZAKONCZONA
│       │   ├── exception/
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   └── SurveyRepository.java
│       │   ├── security/
│       │   │   ├── JwtUtil.java               <- generowanie i walidacja tokenow JWT
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   └── UserDetailsServiceImpl.java
│       │   └── service/
│       │       ├── AuthService.java
│       │       └── SurveyService.java
│       └── resources/
│           └── application.properties         <- konfiguracja aplikacji
└── pom.xml
```

---

## Konfiguracja bazy danych (Microsoft SQL Server)

### Krok 1 - Instalacja SQL Server

Pobierz i zainstaluj **SQL Server 2019/2022 Express** (darmowa wersja):
https://www.microsoft.com/pl-pl/sql-server/sql-server-downloads

Pobierz tez **SQL Server Management Studio (SSMS)**:
https://learn.microsoft.com/pl-pl/sql/ssms/download-sql-server-management-studio-ssms

### Krok 2 - Tworzenie bazy danych

1. Otworz SSMS i polacz sie z serwerem (domyslnie `localhost` lub `.\SQLEXPRESS`)
2. Kliknij prawy przycisk na `Databases` → `New Database`
3. Wpisz nazwe: `SystemAnkiet`
4. Kliknij OK

Alternatywnie uruchom w SSMS:
```sql
CREATE DATABASE SystemAnkiet;
```

### Krok 3 - Wlaczenie logowania SQL Server (sa)

1. W SSMS kliknij prawym na serwer → `Properties` → `Security`
2. Wybierz `SQL Server and Windows Authentication mode`
3. Zrestartuj usluge SQL Server

Resetowanie hasla do konta `sa`:
```sql
ALTER LOGIN sa ENABLE;
ALTER LOGIN sa WITH PASSWORD = 'TwojNowHaslo123!';
```

### Krok 4 - Aktualizacja application.properties

Otworz plik `src/main/resources/application.properties` i zmien:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=SystemAnkiet;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=TwojNowHaslo123!
```

**Jesli uzywasz SQL Server Express**, URL moze byc:
```properties
# Opcja 1: Uzyj nazwanej instancji (bez portu)
spring.datasource.url=jdbc:sqlserver://localhost\SQLEXPRESS;databaseName=SystemAnkiet;encrypt=true;trustServerCertificate=true

# Opcja 2: Uzyj host:port bez instancji (jesli znasz port TCP)
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=SystemAnkiet;encrypt=true;trustServerCertificate=true
```

**Hibernate automatycznie utworzy tabele** przy pierwszym uruchomieniu (`ddl-auto=update`).

---

## Uruchamianie backendu

### Wymagania

- **Java 21** - sprawdz wersje: `java -version`
- **Maven 3.x** - sprawdz wersje: `mvn -version`
- **SQL Server** z baza danych `SystemAnkiet`

### Uruchomienie

**Opcja 1 - Maven Wrapper (zalecane):**
```bash
# Windows
cd Backend
mvnw.cmd spring-boot:run

# Linux/Mac
cd Backend
./mvnw spring-boot:run
```

**Opcja 2 - Maven globalny:**
```bash
cd Backend
mvn spring-boot:run
```

**Opcja 3 - Zbuduj JAR i uruchom:**
```bash
cd Backend
mvnw.cmd package -DskipTests
java -jar target/system-ankiet-backend-0.0.1-SNAPSHOT.jar
```

Backend bedzie dostepny pod adresem: **http://localhost:8080**

---

## Uruchamianie frontendu

### Wymagania

- **Node.js 18+** - pobierz z https://nodejs.org/
- **Angular CLI** - zainstaluj globalnie: `npm install -g @angular/cli`

### Uruchomienie

```bash
cd frontend
npm install
ng serve
```

Frontend bedzie dostepny pod adresem: **http://localhost:4200**

---

## Uruchamianie calej aplikacji

1. Uruchom SQL Server i upewnij sie, ze baza `SystemAnkiet` istnieje
2. Skonfiguruj `application.properties` z prawidlowymi danymi do bazy
3. Uruchom backend: `cd Backend && mvnw.cmd spring-boot:run`
4. Uruchom frontend: `cd frontend && ng serve`
5. Otworz przegladarke: http://localhost:4200

---

## Endpointy REST API

### Autentykacja (publiczne - bez tokenu)

| Metoda | Endpoint | Opis | Request Body |
|--------|----------|------|--------------|
| POST | `/api/auth/register` | Rejestracja uzytkownika | `{ "email", "password", "confirmPassword" }` |
| POST | `/api/auth/login` | Logowanie, zwraca JWT | `{ "email", "password" }` |

**Przyklad odpowiedzi z /api/auth/login:**
```json
{
  "token": "eyJhbGci...",
  "email": "jan@example.com",
  "role": "USER"
}
```

### Ankiety (wymagaja tokenu JWT w naglowku)

Naglowek: `Authorization: Bearer <token>`

| Metoda | Endpoint | Opis |
|--------|----------|------|
| GET | `/api/surveys` | Pobiera liste ankiet zalogowanego uzytkownika |
| POST | `/api/surveys` | Tworzy nowa ankiete |
| DELETE | `/api/surveys/{id}` | Usuwa ankiete (tylko wlasna) |

**Przyklad odpowiedzi z GET /api/surveys:**
```json
[
  {
    "id": 1,
    "title": "Ankieta satysfakcji klienta",
    "status": "Aktywna",
    "responses": 15,
    "createdAt": "2026-06-01T10:00:00"
  }
]
```

**Przyklad ciala POST /api/surveys:**
```json
{
  "title": "Nowa ankieta",
  "description": "Opcjonalny opis"
}
```

---

## Bezpieczenstwo

- Hasla sa szyfrowane algorytmem **BCrypt**
- Autoryzacja oparta na tokenach **JWT** (waznosc: 24 godziny)
- Token nalezy przeslac w naglowku: `Authorization: Bearer <token>`
- Endpointy `/api/auth/**` sa publiczne, pozostale wymagaja tokenu
- CORS skonfigurowany dla frontendu: `http://localhost:4200`

---

## Schemat bazy danych

Hibernate automatycznie tworzy ponizsze tabele:

**users**
| Kolumna | Typ | Opis |
|---------|-----|------|
| id | BIGINT PK | Identyfikator |
| email | NVARCHAR(255) UNIQUE | Adres email (login) |
| password | NVARCHAR(255) | Haslo BCrypt |
| role | NVARCHAR(50) | USER lub ADMIN |
| created_at | DATETIME2 | Data rejestracji |
| active | BIT | Czy konto aktywne |

**surveys**
| Kolumna | Typ | Opis |
|---------|-----|------|
| id | BIGINT PK | Identyfikator |
| title | NVARCHAR(255) | Tytul ankiety |
| description | NVARCHAR(MAX) | Opis |
| status | NVARCHAR(50) | SZKIC / AKTYWNA / ZAKONCZONA |
| created_at | DATETIME2 | Data utworzenia |
| created_by | BIGINT FK | ID uzytkownika |
| responses_count | INT | Liczba odpowiedzi |

---

## Testowanie API (Postman)

1. Zainstaluj Postman: https://www.postman.com/downloads/
2. Zarejestruj uzytkownika:
   - `POST http://localhost:8080/api/auth/register`
   - Body (JSON): `{"email":"test@test.com","password":"haslo123","confirmPassword":"haslo123"}`
3. Zaloguj sie i skopiuj token z odpowiedzi:
   - `POST http://localhost:8080/api/auth/login`
   - Body (JSON): `{"email":"test@test.com","password":"haslo123"}`
4. Uzyj tokenu do chronionego endpointu:
   - `GET http://localhost:8080/api/surveys`
   - Headers: `Authorization: Bearer <skopiowany_token>`
