# Publicar imagen en Docker Hub

Pasos para construir la imagen de **mqbridge** y subirla a Docker Hub cuando quieras publicarla.

## Requisitos

- [Docker](https://docs.docker.com/get-docker/) instalado y en ejecución.
- Cuenta en [Docker Hub](https://hub.docker.com/) (registro gratuito).

---

## 1. Iniciar sesión en Docker Hub

```bash
docker login
```

Introduce tu usuario y contraseña (o un [Access Token](https://hub.docker.com/settings/security) si usas 2FA).

---

## 2. Publicar con el script (recomendado)

Desde la raíz del proyecto:

```bash
./docker-build-push.sh TU_USUARIO_DOCKERHUB
```

Ejemplo si tu usuario es `romeldiaz`:

```bash
./docker-build-push.sh romeldiaz
```

Esto construye la imagen y la sube como `romeldiaz/mqbridge:latest`.

### Publicar con una etiqueta concreta (p. ej. versión)

```bash
TAG=1.0.0 ./docker-build-push.sh romeldiaz
```

La imagen se publicará como `romeldiaz/mqbridge:1.0.0`.

---

## 3. Publicar a mano (sin script)

```bash
# Construir
docker build -t TU_USUARIO/mqbridge:latest .

# Subir a Docker Hub
docker push TU_USUARIO/mqbridge:latest
```

Para una versión concreta:

```bash
docker build -t TU_USUARIO/mqbridge:1.0.0 -t TU_USUARIO/mqbridge:latest .
docker push TU_USUARIO/mqbridge:1.0.0
docker push TU_USUARIO/mqbridge:latest
```

---

## 4. Crear el repositorio en Docker Hub (primera vez)

La primera vez que publiques, puedes crear el repositorio desde la web:

1. Entra en [Docker Hub](https://hub.docker.com/) e inicia sesión.
2. **Create** → **Create Repository**.
3. Nombre: `mqbridge` (o el que uses en la imagen).
4. Visibility: **Public** o **Private**.

Si la imagen tiene el formato `TU_USUARIO/mqbridge`, el push creará el repositorio automáticamente si tienes permisos (en cuentas gratuitas suele ser así).

---

## 5. Comprobar que está publicada

- En el navegador: `https://hub.docker.com/r/TU_USUARIO/mqbridge`.
- O descargar y ejecutar:

```bash
docker pull TU_USUARIO/mqbridge:latest
docker run -p 8081:8081 TU_USUARIO/mqbridge:latest
```

La aplicación quedará disponible en `http://localhost:8081`.
