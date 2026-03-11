# MQ Bridge Service

MQ Bridge Service es un microservicio desarrollado en Sprinboot que actúa como un puente de integración entre aplicaciones HTTP/REST y sistemas de mensajería IBM WebSphere MQ.

Este servicio permite que aplicaciones modernas (como sistemas basados en Laravel) puedan comunicarse con proveedores externos que exponen sus servicios a través de colas IBM MQ, sin necesidad de implementar clientes MQ directamente en dichas aplicaciones.

El microservicio recibe solicitudes mediante API REST, transforma o encapsula los mensajes y los envía a las colas configuradas en IBM MQ, manejando internamente la conexión, envío de mensajes y posibles respuestas.

## Objetivo

El objetivo de este proyecto es desacoplar la lógica de mensajería MQ de las aplicaciones principales, proporcionando un componente especializado que:

Simplifica la integración con IBM WebSphere MQ

Permite a sistemas basados en HTTP/REST interactuar con colas MQ

Centraliza la configuración y manejo de conexiones MQ

Facilita el mantenimiento y evolución de las integraciones con terceros

## Arquitectura

El servicio funciona como un middleware de integración entre aplicaciones REST y el sistema de mensajería MQ.

```
Application (Laravel / API Client)
        │
        │ HTTP / REST
        ▼
MQ Bridge Service (Springboot)
        │
        │ MQ Client
        ▼
IBM WebSphere MQ
        │
        ▼
External Provider
```

## Endpoints

### GET /api/v1/health

Verifica que el servicio esté activo.

```bash
curl http://localhost:8080/api/v1/health
```

Respuesta:
```json
{"status": "UP", "service": "mqbridge"}
```

### POST /api/v1/send

Envía un mensaje a una cola MQ y espera la respuesta.

```bash
curl -X POST http://localhost:8080/api/v1/send \
  -H "Content-Type: application/json" \
  -d '{
    "credentials": {
      "host": "mq.susalud.gob.pe",
      "port": 1414,
      "channel": "DEV.APP.SVRCONN",
      "queue_manager": "QM1",
      "user": "hospital_user",
      "password": "xxxxx"
    },
    "input_queue": "HOSPITAL.REQ",
    "output_queue": "HOSPITAL.RESP",
    "message": "<soap:Envelope>...</soap:Envelope>",
    "options": {
      "retries": 1,
      "retry_wait_ms": 4000,
      "timeout_ms": 10000,
      "format": "xml"
    }
  }'
```

El campo `options` es opcional. Valores por defecto:

| Parámetro | Default | Descripción |
|-----------|---------|-------------|
| `retries` | 1 | Cantidad de reintentos si no hay respuesta |
| `retry_wait_ms` | 4000 | Tiempo de espera (ms) entre reintentos |
| `timeout_ms` | 10000 | Tiempo máximo (ms) de espera por mensaje en cada intento |
| `format` | `string` | Formato del mensaje |

Respuesta exitosa:
```json
{"success": true, "data": "<respuesta del MQ>", "retries": 0}
```

Respuesta con error:
```json
{"success": false, "error": "MQ error (RC=2035 MQRC_NOT_AUTHORIZED): ..."}
```