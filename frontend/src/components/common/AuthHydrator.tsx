import { useEffect } from "react";
import { useAuthStore } from "@/store/auth";

export default function AuthHydrator() {
  useEffect(() => {
    useAuthStore.getState().refreshUser();
  }, []);
  return null;
}
