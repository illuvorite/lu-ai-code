// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 健康检查接口 GET /health/ */
export async function healthCheck(options?: { [key: string]: any }) {
  return request<API.ResultString>('/health/', {
    method: 'GET',
    ...(options || {}),
  })
}
