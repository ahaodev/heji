import { useNavigate, useRouter } from '@tanstack/react-router'
import { Button } from '@/components/ui/button'

export function UnauthorisedError() {
  const navigate = useNavigate()
  const { history } = useRouter()
  return (
    <div className='h-svh'>
      <div className='m-auto flex h-full w-full flex-col items-center justify-center gap-2'>
        <h1 className='text-[7rem] leading-tight font-bold'>401</h1>
        <span className='font-medium'>未授权访问</span>
        <p className='text-muted-foreground text-center'>
          请使用适当的凭据登录 <br /> 以访问此资源。
        </p>
        <div className='mt-6 flex gap-4'>
          <Button variant='outline' onClick={() => history.go(-1)}>
返回
          </Button>
          <Button onClick={() => navigate({ to: '/' })}>返回主页</Button>
        </div>
      </div>
    </div>
  )
}
