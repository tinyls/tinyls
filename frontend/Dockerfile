# Stage 0, "build-stage", based on Node.js, to build and compile the frontend
FROM node:23 AS build-stage

WORKDIR /app

COPY package*.json /app/

RUN npm install

COPY ./ /app/

ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL
ARG VITE_SOCIAL_PERSONAL_URL
ENV VITE_SOCIAL_PERSONAL_URL=$VITE_SOCIAL_PERSONAL_URL
ARG VITE_SOCIAL_GITHUB_URL
ENV VITE_SOCIAL_GITHUB_URL=$VITE_SOCIAL_GITHUB_URL
ARG VITE_SOCIAL_LINKEDIN_URL
ENV VITE_SOCIAL_LINKEDIN_URL=$VITE_SOCIAL_LINKEDIN_URL
ARG VITE_SOCIAL_INSTAGRAM_URL
ENV VITE_SOCIAL_INSTAGRAM_URL=$VITE_SOCIAL_INSTAGRAM_URL

RUN npm ci && npm run build


# Stage 1, based on Nginx, to have only the compiled app, ready for production with Nginx
FROM nginx:1

COPY --from=build-stage /app/dist/ /usr/share/nginx/html

COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY ./nginx-backend-not-found.conf /etc/nginx/extra-conf.d/backend-not-found.conf
