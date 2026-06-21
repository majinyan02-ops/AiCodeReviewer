import request from '@/utils/request';

export type LoginPayload = {
  username: string;
  password: string;
};

export type LoginResult = {
  token: string;
  expireTime: number;
};

export type RegisterPayload = {
  username: string;
  password: string;
  email: string;
};

export type UserInfo = {
  userId: number;
  username: string;
  email: string;
  role: string;
};

export const loginApi = (data: LoginPayload) =>
  request.post<unknown, LoginResult>('/auth/login', data);

export const registerApi = (data: RegisterPayload) => request.post<unknown, void>('/auth/register', data);

export const getUserInfoApi = () => request.get<unknown, UserInfo>('/auth/me');