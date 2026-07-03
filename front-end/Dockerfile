FROM node:24-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM node:24-alpine

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=4200

RUN addgroup -S angular && adduser -S angular -G angular

COPY --from=build /app/dist/mon-projet-angular ./dist/mon-projet-angular
COPY --from=build /app/package*.json ./

RUN npm ci --omit=dev && npm cache clean --force

USER angular:angular

EXPOSE 4200

CMD ["node", "dist/mon-projet-angular/server/server.mjs"]
