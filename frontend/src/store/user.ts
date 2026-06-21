import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { getUserInfoApi, loginApi, type LoginPayload, type UserInfo } from '@/api/auth';

const TOKEN_KEY = 'ai-code-reviewer-token';

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) ?? '');
  const userInfo = ref<UserInfo | null>(null);
  const userInfoLoaded = ref(false);

  const isLogin = computed(() => Boolean(token.value));

  const setToken = (value: string) => {
    token.value = value;
    localStorage.setItem(TOKEN_KEY, value);
  };

  const setUserInfo = (value: UserInfo | null) => {
    userInfo.value = value;
    userInfoLoaded.value = Boolean(value);
  };

  const login = async (payload: LoginPayload) => {
    const result = await loginApi(payload);
    setToken(result.token);
    await loadUserInfo(true);
  };

  const loadUserInfo = async (force = false) => {
    if (!token.value) {
      return null;
    }

    if (userInfoLoaded.value && !force) {
      return userInfo.value;
    }

    const result = await getUserInfoApi();
    setUserInfo(result);
    return result;
  };

  const clearSession = () => {
    token.value = '';
    userInfo.value = null;
    userInfoLoaded.value = false;
    localStorage.removeItem(TOKEN_KEY);
  };

  return {
    token,
    userInfo,
    userInfoLoaded,
    isLogin,
    setToken,
    setUserInfo,
    login,
    loadUserInfo,
    clearSession,
  };
});