<template>
    <PlayGround v-if="$store.state.pk.status === 'playing'" />
    <MatchGround v-if="$store.state.pk.status === 'matching'" />
    <ResultBoard v-if="$store.state.pk.loser != 'none'" />
</template>

<script>
import PlayGround from '../../components/PlayGround.vue'
import MatchGround from '../../components/MatchGround.vue'
import ResultBoard from '../../components/ResultBoard.vue'
import{ onMounted , onUnmounted} from 'vue'
import{useStore} from 'vuex'


export default{
    components:{
        PlayGround,
        MatchGround,
        ResultBoard,
    },
    setup(){
        const store = useStore();
        const socketUrl=`ws://127.0.0.1:3000/websocket/${store.state.user.token}/`;


        let socket = null;
        store.commit("updateOpponent", {
                username: "我的对手",
                photo: "https://img0.baidu.com/it/u=3490576489,1953736046&fm=253&fmt=auto&app=138&f=JPEG?w=200&h=200",
            })

        onMounted(()=>{
            socket=new WebSocket(socketUrl);

            socket.onopen = () => {
                console.log("connected!");//如果连接成功，将socket存储到全局变量中
                store.commit("updateSocket", socket);
            }


            socket.onmessage = msg => {//接收后端返回的消息
                const data = JSON.parse(msg.data);
                 if (data.event === "start-matching") {  // 匹配成功
                    store.commit("updateOpponent", {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                    }, 200);
                    store.commit("updateGame",data.game);//更新Game:包括玩家信息和地图
                }else if (data.event === "move") {
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    const [snake0, snake1] = game.snakes;
                    snake0.set_direction(data.a_direction);
                    snake1.set_direction(data.b_direction);
                } else if (data.event === "result") {
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    const [snake0, snake1] = game.snakes;

                    if (data.loser === "all" || data.loser === "A") {
                        snake0.status = "die";
                    }
                    if (data.loser === "all" || data.loser === "B") {
                        snake1.status = "die";
                    }
                    store.commit("updateLoser", data.loser);
                }
            }


            socket.onclose = () => {
                console.log("disconnected!");
                store.commit("updateStatus", "matching");
            }
        });


        onUnmounted(() => {
            socket.close();
        })

    }

}
</script>


<style scoped>

</style>