<template>
  <div class="login-page" @pointermove="handleLoginPointerMove" @pointerleave="handleLoginPointerLeave">
    <div class="login-page__glow" :style="glowStyle" aria-hidden="true"></div>
    <div class="login-page__grid" aria-hidden="true"></div>

    <el-card class="login-card" shadow="never">
      <div class="login-card__brand">AI Code Reviewer</div>
      <div class="login-card__desc">使用后端账号登录企业级代码审查平台</div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password clearable />
        </el-form-item>
        <el-button class="login-card__submit" type="primary" size="large" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>

      <div class="login-card__extra">
        <span>还没有账号？</span>
        <el-button link type="primary" @click="openRegister">立即注册</el-button>
      </div>
    </el-card>

    <el-dialog v-model="registerVisible" title="注册账号" width="460px">
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="registerForm.username" placeholder="3-50 位用户名" clearable />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model.trim="registerForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" placeholder="至少 6 位密码" show-password clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" :loading="registering" @click="submitRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { registerApi, type LoginPayload, type RegisterPayload } from '@/api/auth';
import { useUserStore } from '@/store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const registerFormRef = ref<FormInstance>();
const loading = ref(false);
const registering = ref(false);
const registerVisible = ref(false);

const mouseX = ref(0);
const mouseY = ref(0);
const glowVisible = ref(false);

const glowStyle = computed(() => ({
  '--glow-x': `${mouseX.value}px`,
  '--glow-y': `${mouseY.value}px`,
  opacity: glowVisible.value ? 1 : 0,
}));

const handleLoginPointerMove = (e: PointerEvent) => {
  mouseX.value = e.clientX;
  mouseY.value = e.clientY;
  glowVisible.value = true;
};

const handleLoginPointerLeave = () => {
  glowVisible.value = false;
};

const form = reactive<LoginPayload>({
  username: '',
  password: '',
});

const registerForm = reactive<RegisterPayload>({
  username: '',
  password: '',
  email: '',
});

const rules: FormRules<LoginPayload> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const registerRules: FormRules<RegisterPayload> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为 3-50 位', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度为 6-100 位', trigger: 'blur' },
  ],
};

onMounted(() => {
  document.documentElement.classList.add('dark');
});

const submit = async () => {
  const valid = await formRef.value?.validate().catch(() => false);

  if (!valid) {
    return;
  }

  loading.value = true;
  try {
    await userStore.login(form);
    ElMessage.success('登录成功');
    await router.replace(String(route.query.redirect ?? '/app/dashboard'));
  } finally {
    loading.value = false;
  }
};

const openRegister = () => {
  registerVisible.value = true;
  Object.assign(registerForm, { username: '', password: '', email: '' });
  registerFormRef.value?.clearValidate();
};

const submitRegister = async () => {
  const valid = await registerFormRef.value?.validate().catch(() => false);

  if (!valid) {
    return;
  }

  registering.value = true;
  try {
    await registerApi(registerForm);
    ElMessage.success('注册成功，请登录');
    form.username = registerForm.username;
    form.password = registerForm.password;
    registerVisible.value = false;
  } finally {
    registering.value = false;
  }
};
</script>
