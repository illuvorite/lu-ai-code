declare namespace API {
  type adminGetAppByIdParams = {
    id: number
  }

  type App = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type AppAddRequest = {
    initPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: number
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: number
  }

  type AppUpdateRequest = {
    id?: number
    appName?: string
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    createTime?: string
    updateTime?: string
    user?: UserVO
  }

  type chatToGenCodeParams = {
    appId: number
    prompt: string
  }

  type DeleteRequest = {
    id?: number
  }

  type getAppVOByIdParams = {
    id: number
  }

  type getMyAppByIdParams = {
    id: number
  }

  type getUserByIdParams = {
    id: number
  }

  type listUserPage1Params = {
    id: number
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userEmail?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type ResultApp = {
    code?: number
    message?: string
    data?: App
    traceId?: string
    timestamp?: number
    success?: boolean
    fail?: boolean
  }

  type ResultAppVO = {
    code?: number
    message?: string
    data?: AppVO
    traceId?: string
    timestamp?: number
    success?: boolean
    fail?: boolean
  }

  type ResultBoolean = {
    code?: number
    message?: string
    data?: boolean
    traceId?: string
    timestamp?: number
    success?: boolean
    fail?: boolean
  }

  type ResultLoginUserVO = {
    code?: number
    message?: string
    data?: LoginUserVO
    traceId?: string
    timestamp?: number
    success?: boolean
    fail?: boolean
  }

  type ResultLong = {
    code?: number
    message?: string
    data?: number
    traceId?: string
    timestamp?: number
    success?: boolean
    fail?: boolean
  }

  type ResultPageAppVO = {
    code?: number
    message?: string
    data?: PageAppVO
    traceId?: string
    timestamp?: number
    success?: boolean
    fail?: boolean
  }

  type ResultPageUserVO = {
    code?: number
    message?: string
    data?: PageUserVO
    traceId?: string
    timestamp?: number
    success?: boolean
    fail?: boolean
  }

  type ResultString = {
    code?: number
    message?: string
    data?: string
    traceId?: string
    timestamp?: number
    success?: boolean
    fail?: boolean
  }

  type ResultUser = {
    code?: number
    message?: string
    data?: User
    traceId?: string
    timestamp?: number
    success?: boolean
    fail?: boolean
  }

  type ResultUserVO = {
    code?: number
    message?: string
    data?: UserVO
    traceId?: string
    timestamp?: number
    success?: boolean
    fail?: boolean
  }

  type ServerSentEventString = Record<string, any>

  type serveStaticResourceParams = {
    deployKey: string
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
    vipExpireTime?: string
    vipCode?: string
    vipNumber?: number
    shareCode?: string
    inviteUser?: number
    userEmail?: string
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userEmail?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userEmail?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userEmail?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userEmail?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userEmail?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
