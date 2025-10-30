import { createRouter, createWebHistory } from "vue-router";
import CommunityList from "@/views/CommunityList.vue";
import RoomDetail from "@/views/RoomDetail.vue";
import ReservationSuccess from "@/views/ReservationSuccess.vue";
import StudentReservations from "@/views/StudentReservations.vue";
const routes = [
    {
        path: "/",
        name: "CommunityList",
        component: CommunityList
    },
    {
        path: "/room/:roomId",
        name: "RoomDetail",
        component: RoomDetail,
        props: true
    },
    {
        path: "/reservation-success",
        name: "ReservationSuccess",
        component: ReservationSuccess
    },

    {
        path: '/student-reservations',
        name: 'StudentReservations',
        component: StudentReservations
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;


