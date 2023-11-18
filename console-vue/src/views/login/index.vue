<script setup>
// 引入Ant Design Vue组件
import {
  Form,
  FormItem,
  Input,
  InputPassword,
  Button,
  message,
  Select,
  ModalhandleLogin
} from 'ant-design-vue'
// 引入Vue相关的方法
import { reactive, ref, unref } from 'vue'
import { fetchLogin, fetchRegister } from '../../service'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import Cookies from 'js-cookie'
const useForm = Form.useForm
// 定义登录表单的初始状态
const formState = reactive({
  usernameOrMailOrPhone: 'shimu',
  password: 'shimu',
  code: ''
})
// 定义全局状态
const state = reactive({
  open: false
})
// 定义登录表单的验证规则
const rulesRef = reactive({
  usernameOrMailOrPhone: [
    {
      required: true,
      message: '请输入用户名或者邮件或者手机号'
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码'
    }
  ]
})
// 使用Form.useForm创建表单
const { validate, validateInfos } = useForm(formState, rulesRef)
// 定义注册表单的初始状态
const registerForm = reactive({
  username: 'shimu',
  password: 'shimu',
  realName: '拾木',
  idType: 0,
  idCard: '4203252001010100',
  phone: '187745712',
  mail: '001@qq.com'
})
// 定义注册表单的验证规则
const registerRules = reactive({
  username: [
    {
      required: true,
      message: '请输入用户名'
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码'
    }
  ],
  realName: [
    {
      required: true,
      message: '请输入姓名'
    }
  ],
  idType: [
    {
      required: true,
      message: '请选择证件类型'
    }
  ],
  idCard: [
    {
      required: true,
      message: '请输入证件号码'
    }
  ],
  phone: [
    {
      required: true,
      message: '请输入电话号码'
    }
  ],
  mail: [
    {
      required: true,
      message: '请输入邮箱'
    }
  ]
})
// 使用Form.useForm创建注册表单
const { validate: registerValidate, validateInfos: registerValidateInfos } =
  useForm(registerForm, registerRules)
// 创建一个ref用于保存当前的操作（登录或注册）
let currentAction = ref('login')
// 获取路由实例
const router = useRouter()
// 处理登录按钮点击事件
const handleFinish = () => {
  if (location.host.indexOf('12306') !== -1) {
    validate()
      .then(() => {
        state.open = true
      })
      .catch((err) => console.log(err))
    return
  }
  validate().then(() => {
    fetchLogin({
      ...formState
    }).then((res) => {
      if (res.success) {
        Cookies.set('token', res.data?.accessToken)
        Cookies.set('username', res.data?.username)
        Cookies.set('userId', res.data?.userId)
        router.push('/ticketSearch')
      } else {
        message.error(res.message)
      }
    })
  })
}
// 处理登录按钮点击事件（带验证码）
const handleLogin = () => {
  if (!formState.code) return message.error('请输入验证码')
  validate()
    .then(() => {
      fetchLogin({
        usernameOrMailOrPhone: formState.usernameOrMailOrPhone,
        password: formState.code
      }).then((res) => {
        if (res.success) {
          Cookies.set('token', res.data?.accessToken)
          Cookies.set('userId', res.data?.userId)
          Cookies.set('username', res.data?.username)
          router.push('/ticketSearch')
        } else {
          message.error(res.message)
        }
      })
    })
    .catch((err) => console.log(err))
}
// 处理注册按钮点击事件
const registerSubmit = () => {
  // 如果当前主机包含 '12306'
  if (location.host.indexOf('12306') !== -1) {
    message.info('hello 12306')
    // 将当前操作设置为登录
    currentAction.value = 'login'
    // 结束函数执行
    return
  }

  // 验证注册表单
  registerValidate()
      .then(() => {
        // 调用 fetchRegister 方法提交注册表单数据
        fetchRegister(registerForm).then((res) => {
          // 如果注册成功
          if (res.success) {
            // 显示注册成功消息
            message.success('注册成功')
            // 将当前操作设置为登录
            currentAction.value = 'login'
            // 更新登录表单的用户名
            formState.usernameOrMailOrPhone = res.data?.username
            // 清空密码字段
            formState.password = ''
          } else {
            // 如果注册失败，显示错误消息
            message.error(res.message)
          }
        })
      })
      .catch((err) => {
        // 如果验证或注册过程中发生错误，输出错误信息到控制台
        console.log(err)
      })
}
</script>

<!-- 页面模板部分 -->
<template>
  <!-- 页面整体结构 -->
  <div class="login-wrapper">
    <div class="title-wrapper">
      <!-- 页面标题 -->
      <!-- <h1 class="title">铁路12306</h1>
      <h3 class="desc">其他文案</h3> -->
    </div>
    <div class="login-reg-panel">
      <div class="login-info-box">
        <h2>已有账号？</h2>
        <h3>欢迎登录账号！</h3>
        <button @click="() => (currentAction = 'login')">去登录</button>
      </div>
      <!-- 注册信息框 -->
      <div class="register-info-box">
        <h2>没有账号？</h2>
        <h3>欢迎注册账号！</h3>
        <button @click="() => (currentAction = 'register')">去注册</button>
      </div>
      <!-- 白色面板，显示登录或注册内容 -->
      <div
        class="white-panel"
        :class="{ 'white-panel-left': currentAction === 'register' }"
      >
        <!-- 登录表单 -->
        <div class="login-show" v-if="currentAction === 'login'">
          <h1 class="title">登录</h1>
          <Form name="basic" autocomplete="off">
            <FormItem v-bind="validateInfos.usernameOrMailOrPhone">
              <Input
                size="large"
                v-model:value="formState.usernameOrMailOrPhone"
                placeholder="用户名"
              >
                <template #prefix
                  ><UserOutlined style="color: rgba(0, 0, 0, 0.25)" /></template
              ></Input>
            </FormItem>

            <FormItem v-bind="validateInfos.password">
              <InputPassword
                size="large"
                v-model:value="formState.password"
                placeholder="密码"
              >
                <template #prefix
                  ><LockOutlined style="color: rgba(0, 0, 0, 0.25)"
                /></template>
              </InputPassword>
            </FormItem>
            <FormItem>
              <div class="action-btn">
                <a href="">忘记密码？</a>
                <!-- 登录按钮 -->
                <Button
                  type="primary"
                  :style="{ backgroundColor: '#202020', border: 'none' }"
                  @click="handleFinish"
                  >登录</Button
                >
              </div>
            </FormItem>
          </Form>
        </div>
        <!-- 注册表单 -->
        <div class="register-show" v-else>
          <!-- 注册表单标题 -->
          <h1 class="title">注册</h1>
          <!-- 表单组件 -->
          <Form name="basic" autocomplete="off" :label-col="{ span: 6 }">
            <!-- 用户名输入框 -->
            <FormItem label="用户名" v-bind="registerValidateInfos.username">
              <Input
                v-model:value="registerForm.username"
                placeholder="请输入用户名"
              >
              </Input>
            </FormItem>
            <!-- 密码输入框 -->
            <FormItem label="密码" v-bind="registerValidateInfos.password">
              <InputPassword
                v-model:value="registerForm.password"
                placeholder="密码"
              >
              </InputPassword>
            </FormItem>
            <!-- 姓名输入框 -->
            <FormItem label="姓名" v-bind="registerValidateInfos.realName">
              <Input
                v-model:value="registerForm.realName"
                placeholder="请输入姓名"
              >
              </Input>
            </FormItem>
            <!-- 证件类型选择框 -->
            <FormItem label="证件类型" v-bind="registerValidateInfos.idType">
              <Select
                :options="[{ value: 0, label: '中国居民身份证' }]"
                v-model:value="registerForm.idType"
                placeholder="请选择证件类型"
              ></Select>
            </FormItem>
            <!-- 证件号码输入框 -->
            <FormItem label="证件号码" v-bind="registerValidateInfos.idCard">
              <Input
                v-model:value="registerForm.idCard"
                placeholder="请输入证件号码"
              >
              </Input>
            </FormItem>
            <!-- 手机号码输入框 -->
            <FormItem label="手机号码" v-bind="registerValidateInfos.phone">
              <Input
                v-model:value="registerForm.phone"
                placeholder="请输入手机号码"
              >
              </Input>
            </FormItem>
            <!-- 邮箱输入框 -->
            <FormItem label="邮件" v-bind="registerValidateInfos.mail">
              <Input
                v-model:value="registerForm.mail"
                placeholder="请输入邮箱账号"
              >
              </Input>
            </FormItem>
            <!-- 注册按钮 -->
            <FormItem>
              <div class="action-btn">
                <a></a>
                <Button
                  type="primary"
                  @click="registerSubmit"
                  :style="{ backgroundColor: '#202020', border: 'none' }"
                  >注册</Button
                >
              </div>
            </FormItem>
          </Form>
        </div>
      </div>
    </div>
  </div>
  <Modal
    :visible="state.open"
    title="人机认证"
    wrapClassName="code-modal"
    width="450px"
    @cancel="state.open = false"
    @ok="handleLogin"
    centered
  >
    <div class="wrapper">
      <div class="code-input">
        <label class="code-label">验证码</label>
        <Input
          v-model:value="formState.code"
          :style="{ width: '300px' }"
        ></Input>
      </div>
    </div>
  </Modal>
</template>

<style lang="scss" scoped>
.login-wrapper {
  width: 100%;
  height: 100%;
  // background-color: #fff;
  background: url('https://nacos.io/img/black_dot.png');
  background-clip: border-box;
  .login-reg-panel {
    position: relative;
    top: 50%;
    transform: translateY(-50%);
    text-align: center;
    width: 40%;
    right: 0;
    left: 20%;
    margin: auto;
    min-width: 800px;
    height: 600px;
    background-color: rgba(30, 30, 30, 0.9);
    .white-panel {
      background-color: rgba(255, 255, 255, 1);
      height: 600px;
      position: absolute;
      width: 50%;
      right: calc(50% - 50px);
      transition: 0.3s ease-in-out;
      z-index: 0;
      box-sizing: border-box;
      .login-show,
      .register-show {
        height: 100%;
        display: flex;
        flex-direction: column;
        // justify-content: space-around;
        transition: 0.3s ease-in-out;
        color: #242424;
        text-align: left;
        padding: 30px;
        .title {
          font-size: 24px;
          font-weight: bolder;
          padding: 20px 0;
          color: #202020;
        }
        .action-btn {
          display: flex;
          width: 100%;
          a {
            display: block;
            line-height: 32px;
          }
          justify-content: space-between;
        }
      }
    }
    .white-panel-left {
      transition: 0.3s ease-in-out;
      right: calc(0px + 50px);
    }
    .login-info-box {
      display: flex;
      flex-direction: column;
      width: 30%;
      padding: 0 50px;
      top: 20%;
      left: 0;
      position: absolute;
      text-align: left;
      justify-content: center;
      font-family: 'Mukta', sans-serif;
      color: #b8b8b8;
      h2 {
        font-size: 24px;
        color: #b8b8b8;
        font-weight: bolder;
        font-weight: bolder;
        margin-bottom: 40px;
      }
      h3 {
        font-size: 20px;
        color: #b8b8b8;
        margin-bottom: 40px;
      }
      button {
        cursor: pointer;
        width: 100%;
        background-color: transparent;
        box-shadow: none;
        border: 1px solid #b8b8b8;
        border-radius: 2px;
        height: 25px;
      }
    }
    .register-info-box {
      width: 30%;
      padding: 0 50px;
      top: 20%;
      right: 0;
      position: absolute;
      text-align: left;
      font-family: 'Mukta', sans-serif;
      color: #b8b8b8;
      h2 {
        font-size: 24px;
        color: #b8b8b8;
        font-weight: bolder;
        font-weight: bolder;
        margin-bottom: 40px;
      }
      h3 {
        font-size: 20px;
        color: #b8b8b8;
        margin-bottom: 40px;
      }
      button {
        cursor: pointer;
        width: 100%;
        background-color: transparent;
        box-shadow: none;
        border: 1px solid #b8b8b8;
        border-radius: 2px;
        height: 25px;
      }
    }
  }
}
.code-modal {
  .wrapper {
    text-align: center;
    .tip-text {
      width: 100%;
      text-align: center;
      font-size: 14px;
      color: red;
    }
    .code-input {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      .code-label {
        margin: 10px;
        &::before {
          content: '*';
          color: red;
        }
      }
    }
  }
}
::v-deep {
  .ant-modal-header {
    background-color: #3b3b3b !important;
  }
  .ant-form-item-label {
    label {
      color: #202020;
    }
  }
}
</style>
