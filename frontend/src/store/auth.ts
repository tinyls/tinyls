import type { UserResponseDTO } from "@/api/schemas";
import { create } from "zustand";
import { getCurrentUser } from "@/api/client/auth-controller/auth-controller";
import { persist } from "zustand/middleware";

interface AuthState {
  user: UserResponseDTO | null;
  isLoggedIn: boolean;
  setUser: (user: UserResponseDTO | null) => void;
  login: (token: string) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isLoggedIn: !!localStorage.getItem("access_token"),
      setUser: (user) => set({ user, isLoggedIn: !!user }),
      login: async (token: string) => {
        localStorage.setItem("access_token", token);
        await get().refreshUser();
      },
      logout: () => {
        localStorage.removeItem("access_token");
        set({ user: null, isLoggedIn: false });
      },
      refreshUser: async () => {
        const token = localStorage.getItem("access_token");
        if (token) {
          try {
            const user = await getCurrentUser();
            set({ user, isLoggedIn: true });
          } catch {
            set({ user: null, isLoggedIn: false });
          }
        } else {
          set({ user: null, isLoggedIn: false });
        }
      },
    }),
    {
      name: "auth",
      partialize: (state) => ({
        user: state.user,
        isLoggedIn: state.isLoggedIn,
      }),
    }
  )
);
