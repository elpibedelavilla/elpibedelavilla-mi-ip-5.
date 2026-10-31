# El Pibe de la Villa — Mi IP (Android TV)

Aplicación gratuita para TV Box / Android TV / Google TV (Android 10+). Consulta **solo al pulsar REFRESH** la IP pública de salida y muestra ciudad, país y bandera. Si se usa una VPN, se consulta la IP de salida de la VPN cuando esta enruta la petición.

## Generar la APK gratis con GitHub

1. Crea un repositorio vacío en GitHub, por ejemplo `elpibedelavilla-mi-ip`.
2. Sube **el contenido de esta carpeta** a la raíz del repositorio (incluida `.github/workflows/build-apk.yml`). Para conservar carpetas ocultas, es recomendable subirlo con Git.
3. Abre la pestaña **Actions** y selecciona **Compilar APK Android TV**. Ejecuta **Run workflow** o haz un push a `main`.
4. Cuando termine, entra en la ejecución y descarga el artefacto `ElPibeDeLaVilla-MiIP-debug`.
5. Descomprime el artefacto e instala `app-debug.apk` en tu TV Box (permitiendo instalación de orígenes desconocidos si procede).

La APK `debug` es apropiada para pruebas y se firma automáticamente con una clave de depuración del entorno de compilación. Para distribuir versiones actualizables de forma estable se necesita firmar las APK de publicación con una **clave privada persistente**.

## Manejo

- Al abrir, no se realiza ninguna consulta.
- Con las flechas del mando, selecciona **REFRESH / ACTUALIZAR IP** y pulsa **OK**.
- Repite cuando cambies entre ISP y VPN.
- **BACK / ESC** sale de la app.

## API y privacidad

Se utiliza `https://ipwho.is/` sin clave para obtener la IP pública y su geolocalización aproximada. La API recibe la IP de salida al realizar la petición. No se almacenan datos ni se envían a servidores propios. La ciudad mostrada puede no coincidir con la ubicación física. El servicio externo tiene condiciones y límites de uso que conviene revisar antes de una distribución amplia. Una VPN con túnel dividido podría enrutar esta app fuera de la VPN.

## Tecnología

Java nativo Android, sin librerías adicionales; `minSdk 29`, `targetSdk 35`. Se utiliza Gradle 8.11.1 y Android Gradle Plugin 8.9.2 en GitHub Actions.

## Recursos

Los recursos gráficos proceden de las imágenes aportadas para este proyecto. El fondo se ha oscurecido y desenfocado para que el texto real de la IP sea legible.
