<template>
  <NavBar />
  <router-view/>
</template>

<script>
import NavBar from './components/NavBar.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"
// import $ from 'jquery'

export default {
  components: { NavBar },
  comments:{
    NavBar
  },
  //setup(){

    // $.ajax({
    //   url: "http://127.0.0.1:3000/user/account/token/",
    //   type: "post",
    //   data: {
    //     username: 'pjh',
    //     password: '31',
    //   },
    //   success(resp) {
    //     console.log(resp);
    //   },
    //   error(resp) {
    //     console.log(resp); 
    //   }
    // });


    // $.ajax({
    //   url: "http://127.0.0.1:3000/user/account/info/",
    //   type: "get",
    //   headers: {
    //     Authorization: "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIzOTliMjBmYzU1NTA0M2UzOTY0MzllOWM5MTFhMGVjNCIsInN1YiI6IjEiLCJpc3MiOiJzZyIsImlhdCI6MTcwODE3NzY1MywiZXhwIjoxNzA5Mzg3MjUzfQ.N1T_fI7p8tmYlI-KaVar0LyUpaVjaVEDtMMO8HAv7Bk",
    //   },
    //   success(resp) {
    //     console.log(resp);
    //   },
    //   error(resp) {
    //     console.log(resp);
    //   }
    // });

    //  $.ajax({
    //    url: "http://127.0.0.1:3000/user/account/register/",
    //    type: "post",
    //    data: {
    //      username: "pjh",
    //      password: "31",
    //      confirmedPassword: "31",
    //    },
    //    success(resp) {
    //      console.log(resp);
    //    },
    //    error(resp) {
    //      console.log(resp);
    //    }
    //  })


  //}
}


</script>


<style>
  body{
    background-image: url("F:/First_BigProject/web/src/assets/images/background.jpg");
    background-size: cover;
  }

</style>
