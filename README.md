# 🚗 KiloMeta — Plataforma Operativa & Financiera para Socios Conductores

<p align="left">
  <img src="https://img.shields.io/badge/Ecosistema-AnAnApps_2.0-blue?style=for-the-badge&logo=android" alt="Ecosistema" />
  <img src="https://img.shields.io/badge/Fase-05_Incubaci%C3%B3n-blue?style=for-the-badge&logo=googleplay" alt="Fase" />
  <img src="https://img.shields.io/badge/Plataforma-Android_Nativa_(Kotlin)-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Plataforma" />
  <img src="https://img.shields.io/badge/Arquitectura-Clean_%2B_Room_%2B_OSMDroid-purple?style=for-the-badge&logo=sqlite" alt="Arquitectura" />
  <img src="https://img.shields.io/badge/Seguridad-Zero_Trust-red?style=for-the-badge&logo=firebase" alt="Seguridad" />
  <img src="https://img.shields.io/badge/Versi%C3%B3n-v2.9.0_alpha.27-00C853?style=for-the-badge" alt="Versión" />
</p>

> **KiloMeta** es una plataforma móvil nativa de alto rendimiento desarrollada bajo el **Ecosistema AnAnApps 2.0 (División de Movilidad y Finanzas Operativas)**. Creada a la medida de la realidad operativa de los socios conductores de plataformas (Uber, DiDi, inDrive, Cabify, Particular) y vehículos bi-combustibles o de alquiler semanal en Colombia.

---

## 🎯 1. VISIÓN DEL PRODUCTO & DOLORES QUE RESUELVE

Los conductores modernos —especialmente aquellos que operan vehículos de alto cilindraje o SUV (como la **BYD S6**) y flotas de alquiler semanal— se enfrentan a puntos ciegos críticos que KiloMeta resuelve con precisión milimétrica:

1. **Cero Porcentajes en Interfaz (Operación 100% en Pesos Colombianos):**  
   Erradicación de cálculos porcentuales confusos y distractores al volante. El conductor ingresa montos tangibles en pesos: *"¿Cuánto cobraste al cliente?"*, *"¿Cuánto te descuentan?"* y la interfaz calcula instantáneamente **"TU GANANCIA LIMPIA"** en tipografía gigante verde esmeralda. Cero distracciones mentales durante la conducción.
2. **Modelo de Doble Bolsillo & Billeteras Separadas por Plataforma (Uber vs. DiDi vs. inDrive):**  
   Separación contable estricta entre el **Efectivo en Mano Líquido** (`CASH_IN_HAND`) y los **Saldos Digitales por Aplicación** (`APP_RETAINED`). Cada plataforma cuenta con su propia billetera estanca (`PlatformWalletBalance`): una deuda por comisiones en efectivo con Uber no canibaliza el saldo a favor digital acumulado en DiDi ni viceversa.
3. **Protocolo Anti-Resurrección de Registros (Zombie Bug Solved):**  
   Arquitectura de borrado atómico bidireccional con tabla de lápidas locales `pending_deletions` en Room SQLite v6 y purga asíncrona en Google Cloud Firestore. Al borrar un viaje, tanqueo o gasto, el motor de sincronización descarta cualquier registro remoto zombi en el `pull`, garantizando que jamás vuelva a aparecer un viaje eliminado.
4. **Sistema de Auto-Actualización In-App OTA (Over-The-Air Update Engine):**  
   Distribución y actualización autónoma desde cabina vehicular sin depender de intermediarios. Consulta remota en Firestore `/app_metadata/kilo_meta`, descarga en segundo plano con barra de progreso y solicitud de instalación desatendida mediante Android `FileProvider`.
5. **Telemetría de Odómetro y Kilómetros en Vacío (Estilo Cockpit KiloMeta):**  
   Formulario lineal y vertical con captura precisa de odómetros. Si el conductor rodó desocupado buscando carrera, KiloMeta detecta y calcula automáticamente los *"km rodados en vacío"* separándolos de los *"km de carrera con pasajero"*.
6. **El Estrés de la "Cuota Semanal" Dinámica:**  
   Prorrateo dinámico de la cuota semanal pactada de alquiler (ej. `$500,000 COP` con día y hora de corte personalizables). La **Bolsa de Reserva Diaria** alimenta el **Semáforo Financiero**, indicando el punto de equilibrio exacto del día.
7. **Costo Real de Combustible en Surtidor y Eficiencia Bi-Combustible:**  
   Auditoría de gasto monetario real desembolsado en surtidor (GNV vs. Gasolina), calculando el costo por kilómetro y el ahorro acumulado frente al uso de gasolina corriente para vehículos a gas.
8. **Navegación Ergonómica de Cabina Estilo Cockpit KiloMeta (4 Pestañas + Speed Dial):**  
   Barra inferior en la *Thumb Zone* (Ruta, Informes, Alertas, Más) y botón flotante central (+) elevado de 56dp con menú radial para registrar Carreras, Tanqueos y Gastos en $\le 3$ toques.
9. **Lugares Frecuentes ("Mis Bombas & Talleres") con Mapas Nativos OSMDroid:**  
   Catálogo personal de estaciones de servicio, talleres, lavaderos y parqueaderos con renderizado 100% nativo en Android Canvas con **OSMDroid 6.1.20** (cero WebViews lentos, cero APIs comerciales) y lanzador directo de 1 tap a **Waze** y **Google Maps**.
10. **Respaldo Asíncrono Anti-Hurto y Recuperación ante Desastres:**  
    Persistencia soberana en Room SQLite combinada con replicación desatendida en Google Cloud Firestore bajo reglas criptográficas Zero-Trust (`request.auth.uid == userId`) y resolución Last-Write-Wins (LWW).
11. **Edición Integral en Cabina & Odometría Continua:**  
    Modificación completa de carreras (odómetros, plataforma, método de pago, cobrado y comisión), tanqueos y gastos sin perder el historial ni reiniciar a cero las billeteras digitales. Recálculo reactivo en tiempo real de kilómetros recorridos y caja líquida.
12. **Auto-Captura Heurística de Recibos OCR (Uber & DiDi) con Subsidios Positivos:**  
    Escaneo instantáneo de pantallazos de viaje. DiDi captura ganancias netas, horas, distancia, cobro en mano y maneja con fidelidad cuando DiDi aporta dinero extra por promociones (saldo positivo a favor en cartera). Uber prioriza la tarifa real incrementada de cabecera sobre la tarifa base preliminar.
13. **Métricas y Semáforo de Rentabilidad por Carrera en Historial:**  
    Cálculo en tiempo real de tarifa lograda por kilómetro ($\$/km$), proyección horaria ($\$/h$) y veredicto con semáforo vehicular (🟢 Rentable, 🟡 Equilibrio, 🔴 No rentable) directamente en la ficha técnica expandible de cada carrera en el Historial.
14. **Cabina 100% Soberana y Silenciosa:**  
    Lectura pasiva de pantallas en segundo plano completamente desactivada para erradicar cualquier cuadro emergente invasivo mientras el conductor opera.

---

## 🧠 2. ARQUITECTURA TÉCNICA & STACK TECNOLÓGICO

KiloMeta está construida bajo los estándares de ingeniería más avanzados de Google y las pautas del Ecosistema AnAnApps v2.0:

* **Lenguaje:** Kotlin 2.0.21 (tipado estricto, null-safety absoluto, Compose Compiler plugin integrado).
* **Patrón de Arquitectura:** **Clean Architecture Modular**:
  * `domain/`: Modelos puros inmutables (`PlatformWalletBalance`, `CashSettlementSummary`), contratos de repositorios, UseCases e interactores financieros, de lugares y mantenimiento.
  * `data/`: Repositorios desacoplados, Room SQLite 2.6.1 (esquema relacional v6 con `PendingDeletionEntity`, `MIGRATION_5_6`, integridad en cascada y migraciones deterministas), DataSources remotos Firebase (`FirebaseAuthDataSource`, `FirestoreDataSource`), workers en background y mappers puros.
  * `presentation/`: Interfaz reactiva 100% Jetpack Compose Material 3 con patrón Unidirectional Data Flow (*UDF*) y `StateFlow`.
  * `core/`: Utilidades contables, sanitización contra inyección de fórmulas (`CsvSanitizer`), monitores de conectividad, instalador de APKs (`ApkInstallerHelper`) y módulos Hilt.
* **Mapas Nativos & Cartografía:** **OSMDroid 6.1.20** (`org.osmdroid:osmdroid-android`) renderizado nativamente sobre Android Canvas/OpenGL. Libre de WebViews, sin consumo de créditos de Google Maps API y con soporte de cache offline local.
* **Single Source of Truth (SSOT):** **Room SQLite local** es la fuente inmutable de la aplicación. La interfaz de usuario jamás consulta fuentes remotas de forma síncrona.
* **Inyección de Dependencias:** **Hilt 2.52 (Dagger)** con integración KSP y `HiltWorkerFactory` para background workers.
* **Procesamiento en Segundo Plano:** **Android Jetpack WorkManager 2.9.1** con tareas periódicas para sincronización en la nube (`SyncWorker`) y notificaciones de corte vehicular (`WeeklyCutoffWorker`).
* **Ciberseguridad DataGuard Tier 3:**
  * Reglas Zero-Trust en `firestore.rules` con aislamiento hermético por UID y lectura pública desautenticada protegida para metadatos OTA.
  * Protocolo *Clean Session Handshake* con purga atómica de 3 niveles al cerrar sesión.
  * Mínimo privilegio de red: permisos exclusivos `INTERNET`, `ACCESS_NETWORK_STATE` y `REQUEST_INSTALL_PACKAGES` (para OTA).
  * Cero permisos invasivos de ubicación en segundo plano (`ACCESS_BACKGROUND_LOCATION` no requerido).
  * Sanitización contra ataques CWE-1236 en exportaciones CSV.
  * `allowBackup="false"` y componentes internos no exportados (`exported="false"`).

---

## 🎨 3. SISTEMA DE DISEÑO: COCKPIT DARK & BENCHMARK DRIVVO

El sistema de diseño de KiloMeta está optimizado para la cabina vehicular bajo condiciones de alta vibración, luz solar intensa y turnos nocturnos:

* **Paleta Cockpit Deep Obsidian:**  
  Fondo `#0B0E14`, Superficies `#151B26` / `#1E2638`, Texto principal `#E6EDF3` (reduce el cansancio ocular y previene reflejos en el parabrisas).
* **Barra Inferior Ergonómica Estilo Cockpit KiloMeta:**
  * 🚗 **Ruta:** Timeline continuo en vivo, odómetro inteligente, tarjeta Hero de dinero neto en bolsillo, card de liquidación de efectivo y billeteras separadas, y feed reactivo de actividad.
  * 📊 **Informes:** Analíticas por plataforma, balance bi-combustible (GNV vs. Gasolina), histórico de turnos y exportador contable CSV.
  * 🔔 **Alertas:** Notificaciones de vencimiento de cuota, recordatorios de mantenimiento preventivo y alertas mecánicas.
  * ⚙️ **Más:** Perfil del conductor, especificaciones del vehículo, Mis Bombas & Talleres, gastos en ruta, diagnóstico de nube, actualizador OTA y cierre de sesión.
  * ➕ **Speed Dial Central Elevado:** Botón flotante central de 56dp elevado 16dp con selector radial para Carrera, Tanqueo y Gasto rápido en 1 segundo.
* **Semáforo Dinámico de Equilibrio Financiero:**
  * 🔴 **Rojo (`#FF4D4D`):** Ganancia del día inferior a los costos operativos (cuota prorrateada + combustible quemado).
  * 🟡 **Amarillo (`#FFB800`):** Zona de amortización cercana al punto de equilibrio (75% cubierto).
  * 🟢 **Verde ("Turno Libre" - `#00E676`):** Punto de equilibrio superado al 100%. Todo ingreso subsiguiente es ganancia pura.
* **Tokens Visuales de Plataformas (`PlatformTheme`):**
  * **Uber:** Contenedor `#1C1C1E` con borde `#3A3A3C`, monograma `UB` en blanco.
  * **DiDi:** Contenedor `#FF7A00` (Naranja corporativo), monograma `DD` en blanco.
  * **inDrive:** Contenedor `#B4F309` (Lima neón inDrive), monograma `IN` en negro puro (`#000000`) para contraste solar certificado WCAG AAA (14.2:1).
  * **Particular:** Contenedor `#7B1FA2` (Púrpura distintivo), monograma `PR` en blanco.
* **Ergonomía Vehicular (Thumb Zone):** Touch targets táctiles $\ge 56\,\text{dp}$, feedback háptico y campos con teclado numérico ampliado.

---

## 📂 4. ESTRUCTURA DEL REPOSITORIO (CANÓNICA ANANAPPS V2.0)

```text
KiloMeta/
├── BITACORAS/                 # Bitácora maestra inmutable de eventos y decisiones
│   └── BITACORA.md
├── CODIGO/                    # Proyecto Android Nativo (Gradle, Kotlin 2.x, Compose)
│   ├── app/
│   │   ├── google-services.json.template  # Plantilla sanitizada para CI/CD
│   │   ├── src/main/java/com/ananapps/kilometa/
│   │   │   ├── core/          # Utilitarios, sanitización CSV, OTA installer, red
│   │   │   ├── data/          # Room SQLite (v6), DAOs, Entities, Firebase, Workers, Mappers
│   │   │   ├── domain/        # Modelos puros, UseCases (Financieros, Mantenimiento, Lugares, Auth, Sync, OTA)
│   │   │   └── presentation/  # UI Jetpack Compose
│   │   │       ├── auth/          # Login Gatekeeper, autenticación Google y Correo
│   │   │       ├── common/        # Badges de nube, tokens de plataforma, diálogos comunes
│   │   │       ├── expenses/      # Gestión independiente de gastos operativos en ruta
│   │   │       ├── history/       # Analíticas multi-plataforma, ahorro bi-combustible, exportador CSV
│   │   │       ├── home/          # Cockpit continuo, tarjeta Hero, feed, KiloMeta sheets, billeteras
│   │   │       ├── maintenance/   # Mantenimiento preventivo estilo Cockpit KiloMeta, alertas de odómetro
│   │   │       ├── more/          # Menú central "Más", OTA updater, accesos a subsistemas y perfil
│   │   │       ├── navigation/    # KiloMetaBottomBar, TopBar vehicular, AppNavigation
│   │   │       ├── places/        # Mis Bombas & Talleres, OSMDroid nativo, Waze/Maps launcher
│   │   │       ├── profile/       # Perfil del conductor, corte dinámico, catálogo vehicular
│   │   │       ├── sync/          # Panel de diagnóstico de nube, sincronización forzada
│   │   │       └── theme/         # Deep Obsidian tokens, tipografía, paleta vehicular
│   │   └── AndroidManifest.xml
│   ├── firestore.rules        # Reglas Zero-Trust de Cloud Firestore
│   ├── gradle/
│   │   └── libs.versions.toml # Version Catalogs centralizado
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── DOCUMENTACION/             # Guías maestras, contexto y manuales
│   ├── AGENTS.md              # Reglas sagradas de negocio y directrices técnicas para agentes
│   ├── VISION_DEL_PROYECTO.md # Manifiesto estratégico y hoja de ruta del producto
│   ├── IDENTIDAD_VISUAL/      # Activos visuales e ícono oficial KM Vector Gauge
│   └── PROPUESTA_ARQUITECTURA_ORIGINAL.md
├── EVIDENCIAS/                # Capturas de pantallas, métricas y validaciones en ruta
├── ORDENES_DE_GERENCIA/       # Directivas ejecutivas canónicas autorizadas
├── PLANES_DE_IMPLEMENTACION/  # Planes técnicos paso a paso de cada orden
├── REPORTES/                  # Informes de auditoría y certificación QA
└── README.md                  # Este documento canónico
```

---

## 📈 5. MATRIZ DE ÓRDENES Y ESTADO DEL PROYECTO

| Orden | Hito / Descripción Técnica | Versión | Estado |
| :--- | :--- | :--- | :--- |
| **Orden #001** | Génesis del Proyecto, Graduación a Incubación y Reglas de Negocio | `v0.0.1` | 🟢 **COMPLETADO** |
| **Orden #002** | Scaffolding Android Base, Clean Architecture, Hilt, Compose y DataGuard | `v0.1.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #003** | Persistencia Relacional Room SQLite con Integridad Referencial en Cascada | `v0.2.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #004** | Motor de Semáforo Dinámico, Reloj de Cuota Semanal y Cockpit UI | `v0.3.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #005** | Historial de Turnos, Analíticas Multi-Plataforma y Ahorro Bi-Combustible | `v0.4.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #006** | Exportación de Reportes Financieros (CSV SAF con UTF-8 BOM + WhatsApp) | `v0.5.0-alpha.1` | 🟢 **COMPLETADO — CIERRE FASE 1** |
| **Orden #007** | Motor Cloud Sync & Respaldo Anti-Robo / Desastre (Firebase Auth + Firestore) | `v0.6.0-alpha.1` | 🟢 **COMPLETADO — INICIO FASE 2** |
| **Orden #008** | Vinculación y Conexión Real con Firebase (Google Services Plugin & Ping) | `v0.7.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #009** | Motor de Conflictos Offline-Online & Sincronización con WorkManager (LWW) | `v0.8.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #010** | Dashboard de Auditoría Contable Avanzada, Notificaciones de Cuota Privadas | `v0.9.0-alpha.1` | 🟢 **COMPLETADO — CIERRE FASE 2** |
| **Orden #011** | Login Gatekeeper Mandatorio, Perfil Multi-Propulsión e Ícono Oficial | `v1.0.0-rc2` | 🟢 **COMPLETADO** |
| **Orden #012** | Descongestión de TopAppBar, Refactor del Perfil y Segregación Conductor/Vehículo | `v1.0.1-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #013** | Motor de Cuota Dinámica para Arrendados y Liquidación Doble Bolsillo | `v1.0.2-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #014** | Módulo de Mantenimiento Preventivo Estilo Cockpit KiloMeta y Gastos en Ruta | `v1.1.0-alpha.1` | 🟢 **COMPLETADO — CIERRE FASE 4** |
| **Orden #015** | Onboarding Obligatorio de Cabina, Selector de Corte Vehicular y Dev Reset | `v1.2.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #016** | Sincronización Dinámica de Corte Vehicular en Cabina y Formato de Horas | `v1.2.1-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #017** | Aislamiento Multicuenta, Clean Session Handshake y Purga Atómica Local | `v1.3.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #018** | Reestructuración Visual de Cabina, Timeline en Vivo y Adopción Estándar Cockpit KiloMeta | `v1.4.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #019** | Desacoplamiento de Registro sin Turno, Combustible Real y Cruce Billetera | `v1.5.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #020** | "Mis Bombas & Talleres" (Lanzador Waze/Maps) y Catálogo Vehicular Colombia | `v1.6.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #021** | Timeline Continuo Estilo Cockpit KiloMeta, Odómetro Inteligente y Selector GPS | `v1.7.0-alpha.1` | 🟢 **COMPLETADO** |
| **Hotfix #021.1** | Empaquetado Offline Local de Leaflet en Assets (Parche Temporal) | `v1.7.1-alpha.1` | 🟢 **COMPLETADO** |
| **Hotfix #021.2** | Erradicación Definitiva de WebViews y Migración Nativa a OSMDroid | `v1.7.2-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #022** | Vista General de Mapa OSMDroid con Todos los Lugares y Tarjeta Flotante | `v1.8.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #023** | Diferenciación Visual de Plataformas (Colores Oficiales, Monogramas y Badges) | `v1.8.1-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #024** | Motor de Cálculo Exacto de Comisiones de Plataformas (Dual Capture & Calibration) | `v1.9.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #025** | Sistema de Auto-Actualización In-App OTA (Over-The-Air Update Engine) | `v1.9.0-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #026** | Simplificación Radical de Registro Estilo Cockpit KiloMeta y Telemetría de Odómetro (Room v5) | `v1.9.1-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #026.1**| Fidelidad Absoluta a KiloMeta, Tríada de Pagos (Nequi/Efectivo/App) y Fix OTA Firestore | `v1.9.2-alpha.1` | 🟢 **COMPLETADO** |
| **Orden #027** | Erradicación del Zombie Bug (Room v6 Lápidas + Purga Firestore), Cero % y Multi-Billetera | `v1.9.3-alpha.1` | 🟢 **COMPLETADO** |

---

## 🔮 6. PRÓXIMOS PASOS TÉCNICOS & RUTA DE ESTABILIZACIÓN

Tras la exitosa consolidación de la **Orden #027**, KiloMeta avanza hacia su fase de certificación en campo:

1. **Validación Operativa en Jornada Real:**  
   Pruebas de campo con chofer en vivo operando simultáneamente Uber, DiDi e inDrive en Bogotá para verificar el comportamiento de la tarjeta de liquidación con billeteras independientes y la estabilidad de las eliminaciones atómicas en Firestore.
2. **Módulo de Métricas y Rendimiento Operativo (Dashboard BI):**  
   Consolidación de estadísticas de ganancia neta real por hora rodada y por kilómetro recorrido para comparar objetivamente la rentabilidad entre aplicaciones.
3. **Distribución Beta Cerrada mediante OTA:**  
   Despliegue de versiones sucesivas a través del canal OTA para conductores seleccionados de la flota.

---

## 🤖 7. DIRECTRICES SAGRADAS PARA AGENTES & CONTRIBUIDORES

1. **Principio de LUZ VERDE:** Queda prohibida cualquier modificación de código en `CODIGO/` sin contar con una Orden de Gerencia explícita y su plan de implementación aprobado.
2. **Modularidad Estricta (< 280 Líneas):** Ningún archivo de código fuente (`.kt`) debe superar las 280 líneas. Si un componente crece, debe modularizarse inmediatamente en subcomponentes desacoplados.
3. **Room SQLite como SSOT:** La UI solo se comunica con StateFlow provenientes de la base de datos local. Cualquier sincronización con Firestore o APIs externas ocurre de forma asíncrona y desatendida.
4. **Cero Mentiras Técnicas:** Nunca simular componentes sin señalizarlos con advertencias explícitas. Todo dato mostrado al usuario debe provenir de cálculos reales o persistencia comprobada.
5. **Ergonomía Vehicular Inviolable:** Touch targets táctiles $\ge 56\,\text{dp}$, contraste solar certificado (WCAG AAA en texto clave) y cero dependencias de conexión a internet para la operación cotidiana en carretera.
6. **Operación en Moneda Real (Cero Porcentajes Confusos):** Todo formulario de registro debe operar directamente en moneda local ($ COP) para aliviar la carga cognitiva del conductor en ruta.

---
*KiloMeta — Impulsado por el Ecosistema AnAnApps 2.0*  
*Custodiado por el Documentador Principal y Custodio del Conocimiento.*
