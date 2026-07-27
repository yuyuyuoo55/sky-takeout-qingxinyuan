FROM node:20-alpine

WORKDIR /app
COPY server.js .
COPY dist ./dist

ENV MERCHANT_HOST=0.0.0.0
ENV MERCHANT_PORT=8888

EXPOSE 8888
CMD ["node", "server.js"]
