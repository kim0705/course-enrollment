import { useQuery } from '@tanstack/react-query';
import { getUserList } from '../api/user';

/* 유저 목록 조회를 위한 커스텀 훅 */
export const useUserList = () => {
    return useQuery({
        queryKey: ['userList'],
        queryFn: () => getUserList().then(res => res.data),
    });
};
