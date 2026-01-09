import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";
import { useAuthStore } from "@/stores/authStore";
import { accountingRoutes } from "@/modules/accounting/routes";
import { invoicesRoutes } from "@/modules/invoices/routes";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "home",
    component: () => import("@/pages/HomePage.vue"),
  },
  {
    path: "/authorized",
    name: "authorized",
    component: () => import("@/pages/AuthorizedPage.vue"),
    meta: { isCallback: true },
  },
  {
    path: "/dashboard",
    name: "dashboard",
    component: () => import("@/pages/DashboardPage.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/accounting",
    children: accountingRoutes,
    meta: { requiresAuth: true },
  },
  {
    path: "/invoices",
    children: invoicesRoutes,
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore();

  if (authStore.isLoading) {
    await new Promise<void>((resolve) => {
      const unwatch = authStore.$subscribe(() => {
        if (!authStore.isLoading) {
          unwatch();
          resolve();
        }
      });
      setTimeout(() => {
        unwatch();
        resolve();
      }, 5000);
    });
  }

  if (to.meta.isCallback) {
    next();
    return;
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    sessionStorage.setItem("auth_return_url", to.fullPath);
    next({ name: "home" });
    return;
  }

  next();
});

export default router;
