# ─────────────────────────────────────────
# STAGE 1: Build con Node
# ─────────────────────────────────────────
FROM node:22-alpine AS builder

WORKDIR /app

# Copiar dependencias primero (cache)
COPY package.json package-lock.json ./
RUN npm ci

# Copiar código y construir
COPY . .
ARG VITE_API_URL=/api/v1
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# ─────────────────────────────────────────
# STAGE 2: Servir con Nginx
# ─────────────────────────────────────────
FROM nginx:alpine

# Copiar build de Vite
COPY --from=builder /app/dist /usr/share/nginx/html

# Configuración Nginx para React Router (SPA)
RUN echo 'server { \
    listen 80; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    location /api { \
        proxy_pass http://backend:8080; \
        proxy_set_header Host $host; \
        proxy_set_header X-Real-IP $remote_addr; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]