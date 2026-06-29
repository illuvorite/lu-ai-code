// 根据后端接口生成前端请求和 TS 模型代码
import path from 'path'

// __dirname 在 jiti 加载时解析为项目根目录
const specPath = path.resolve(__dirname, 'openapi-spec.json')

export default {
  // 请求库导入路径
  requestLibPath: "import request from '@/request'",
  // OpenAPI/Swagger 文档地址（使用本地文件避免 HTTP 请求卡死）
  schemaPath: specPath,
  // 生成代码的输出目录
  serversPath: './src',
  // TypeScript 命名空间（对应 typings.d.ts 中的 declare namespace）
  namespace: 'API',
  // 请求函数使用小驼峰命名
  isCamelCase: true,
  // 自定义 hooks
  hook: {
    // 将 OpenAPI 中文标签映射为英文文件名
    customFileNames: (operationObject: { tags?: string[] }) => {
      const tagMap: Record<string, string> = {
        '用户接口': 'userController',
        '应用接口': 'appController',
        '健康检查': 'healthController',
        'static-resource-controller': 'staticResourceController',
      }
      const tags = operationObject.tags
      if (tags && tags.length > 0) {
        return tags.map((t: string) => tagMap[t] || t)
      }
      // 返回 undefined 则使用默认逻辑
      return undefined
    },
  },
}
