<template>
    <content-field>
      <table class="table table-striped table-hover" style="text-align: center;">
          <thead>
              <tr>
                  <th>玩家</th>
                  <th>天梯积分</th>
              </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
                    <td>
                        <img :src="user.photo" alt="" class="user-photo">
                        &nbsp;<!--空格-->
                        <span class="user-username">{{ user.username }}</span>
                    </td>
                    <td>
                        {{ user.rating }}
                    </td>
                </tr>
          </tbody>
      </table>
      <nav aria-label="...">
            <ul class="pagination" style="float: right;">
                <li class="page-item" @click="click_page(-2)">
                    <a class="page-link" href="#">前一页</a>
                </li>
                <li :class="'page-item ' + page.is_active" v-for="page in pages" :key="page.number" @click="click_page(page.number)">
                    <a class="page-link" href="#">{{page.number}}</a>
                </li>
                <li class="page-item" @click="click_page(-1)">
                    <a class="page-link" href="#">后一页</a>
                </li>
            </ul>
        </nav>
    </content-field>
</template>

<script>
import ContentField from '../../components/ContentField.vue';
import { useStore } from 'vuex';
import { ref } from 'vue';
import $ from 'jquery';

export default{
  components: { ContentField },

  setup() {
      const store = useStore();
      let current_page = 1;
      let users = ref([]);
      let total_users = 0;
      let pages = ref([]);


      const click_page = page => {
            if(page === -2) page = current_page - 1;
            else if(page === -1) page = current_page + 1;
            let max_pages = parseInt(Math.ceil(total_users / 3));

            if(page >= 1 && page <= max_pages) {
                pull_page(page);//加载一个新的分页
            }
        }

        const update_pages = () => {
            let max_pages = parseInt(Math.ceil(total_users / 3));//最大页数
            let new_pages = [];
            for(let i = current_page - 2; i <= current_page + 2; i++) {//返回可以显示出来的页面数，最三个选项
                if(i >= 1 && i <= max_pages) {
                    new_pages.push({
                        number: i,
                        is_active: i === current_page ? "active" : "",//当前页面加标注
                    });
                }
            }
            pages.value = new_pages;
        };

      const pull_page = page => {//获取对局列表
        current_page=page;
          $.ajax({
              url: "http://127.0.0.1:3000/ranklist/getlist/",
              data: {
                  page,
              },
              type: "get",
              headers: {
                  Authorization: "Bearer " + store.state.user.token,
              },
              success(resp) {
                console.log(resp);
                users.value=resp.users;
                total_users = resp.users_count;
                update_pages();
              },
              error(resp) {
                  console.log(resp);
              }
          })
      };
      pull_page(current_page);

      return{
        users,
        pages,
        click_page,
      }

  }
}

</script>


<style scoped>
img.user-photo {
    width: 4vh;
    border-radius: 50%;
}

</style>