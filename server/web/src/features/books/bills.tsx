import { getRouteApi, Link } from '@tanstack/react-router'
import { ChevronLeft } from 'lucide-react'
import { ConfigDrawer } from '@/components/config-drawer'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { BillsTable } from './components/bills-table'
import { useBills } from './hooks/use-bills'
import { useBooks } from './hooks/use-books'

const route = getRouteApi('/_authenticated/books/$bookId')

export function BookBills() {
  const { bookId } = route.useParams()
  const search = route.useSearch()
  const navigate = route.useNavigate()

  const { data: books } = useBooks()
  const bookName =
    books?.find((b) => b._id === bookId)?.name ?? '账本'

  const queryParams = {
    page: search.page || 1,
    page_size: search.pageSize || 10,
  }

  const { data: billsData, isLoading, error } = useBills(bookId, queryParams)

  const navigateWrapper = ({
    search: searchUpdate,
    replace,
  }: {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    search: any
    replace?: boolean
  }) => {
    navigate({ search: searchUpdate, replace })
  }

  return (
    <>
      <Header fixed>
        <Search />
        <div className='ms-auto flex items-center space-x-4'>
          <ThemeSwitch />
          <ConfigDrawer />
          <ProfileDropdown />
        </div>
      </Header>

      <Main>
        <div className='mb-2 flex flex-wrap items-center justify-between space-y-2'>
          <div className='flex items-center gap-2'>
            <Button variant='ghost' size='icon' className='size-8' asChild>
              <Link to='/books'>
                <ChevronLeft className='size-4' />
              </Link>
            </Button>
            <div>
              <h2 className='text-2xl font-bold tracking-tight'>
                {bookName}
              </h2>
              <p className='text-muted-foreground'>
                浏览该账本下的所有账单记录。
              </p>
            </div>
          </div>
        </div>
        <div className='-mx-4 flex-1 overflow-auto px-4 py-1 lg:flex-row lg:space-y-0 lg:space-x-12'>
          {isLoading ? (
            <div className='space-y-4'>
              <div className='flex items-center justify-between'>
                <Skeleton className='h-8 w-48' />
                <Skeleton className='h-10 w-32' />
              </div>
              <div className='space-y-2'>
                {Array.from({ length: 5 }).map((_, i) => (
                  <Skeleton key={i} className='h-16 w-full' />
                ))}
              </div>
            </div>
          ) : error ? (
            <div className='flex h-32 items-center justify-center text-muted-foreground'>
              加载账单数据失败，请重试
            </div>
          ) : (
            <BillsTable
              data={billsData?.list || []}
              search={search}
              navigate={navigateWrapper}
              totalCount={billsData?.total || 0}
            />
          )}
        </div>
      </Main>
    </>
  )
}
