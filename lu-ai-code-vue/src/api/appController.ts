// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 创建应用 POST /app/add */
export async function addApp(body: API.AppAddRequest, options?: { [key: string]: any }) {
  return request<API.ResultLong>('/app/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员删除应用 POST /app/admin/delete */
export async function adminDeleteApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/app/admin/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员查看应用详情 GET /app/admin/get */
export async function adminGetAppById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.adminGetAppByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultApp>('/app/admin/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 管理员分页查询应用列表 POST /app/admin/list/page */
export async function adminListApps(body: API.AppQueryRequest, options?: { [key: string]: any }) {
  return request<API.ResultPageAppVO>('/app/admin/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员更新应用 POST /app/admin/update */
export async function adminUpdateApp(
  body: API.AppAdminUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>('/app/admin/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 应用聊天生成代码（流式 SSE） GET /app/chat/gen/code */
export async function chatToGenCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.chatToGenCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ServerSentEventString[]>('/app/chat/gen/code', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 删除自己的应用 POST /app/delete */
export async function deleteMyApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/app/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 应用部署 POST /app/deploy */
export async function deployApp(body: API.AppDeployRequest, options?: { [key: string]: any }) {
  return request<API.ResultString>('/app/deploy', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 查看自己的应用详情 GET /app/get */
export async function getMyAppById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getMyAppByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultApp>('/app/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 根据 id 获取应用详情 GET /app/get/vo */
export async function getAppVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppVOByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultAppVO>('/app/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 分页查询精选应用列表 POST /app/list/featured/page */
export async function listFeaturedApps(
  body: API.AppQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageAppVO>('/app/list/featured/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页查询自己的应用列表 POST /app/list/my/page */
export async function listMyApps(body: API.AppQueryRequest, options?: { [key: string]: any }) {
  return request<API.ResultPageAppVO>('/app/list/my/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 修改自己的应用 POST /app/update */
export async function updateMyApp(body: API.AppUpdateRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/app/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
