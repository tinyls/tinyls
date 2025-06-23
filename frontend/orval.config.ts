import { defineConfig } from "orval";

export default defineConfig({
  tinyls: {
    input: {
      target: "./openapi.json",
    },
    output: {
      workspace: "./src",
      target: "./api/client",
      schemas: "./api/schemas",
      client: "react-query",
      mode: "tags-split",
      mock: false,
      override: {
        mutator: {
          path: "./api/client/mutator/customAxiosInstance.ts",
          name: "customInstance",
        },
      },
    },
  },
});
