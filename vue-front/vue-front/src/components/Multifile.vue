<template>
    <div>
      <!-- <form action="http://localhost:8086/fileUpload" method="post" enctype="multipart/form-data">
          <p>选择文件: <input type="file" name="file"/></p>
          <p><input type="submit" value="提交"/></p>
      </form>
      <form action="http://localhost:8086/fileDownload" method="post"  target="_top" class="form form-horizontal" >  
        <input type="text" class="input-text"   id="filename" name="filename" style="width:250px"  required="required">  
        <input class="btn btn-primary radius" type="submit" id="submit" value="  下载  ">  
      </form> 
      <form method="POST" enctype="multipart/form-data"
      action="http://localhost:8086/batch/upload">
        File to upload: <input type="file" name="file"><br />
        File to upload: <input type="file" name="file"><br />
        <input type="submit" value="Upload"> Press here to upload the file!
      </form> -->

      <br>
      <br>
      <div>
<!--         <span  style="color:red;">文件授权系统测试-表单提交</span>
         <form action="http://localhost:8899/uploadFile" method="post" enctype="multipart/form-data">
            <p>选择文件: <input type="file" name="file"/></p>
            <p>目录: <input type="text" name="directory"/></p>
            <p><input type="submit" value="提交"/></p>
          </form>
        <hr> -->
        <br>
        <span style="color:red;">所有目录信息获取-ajax提交</span>
        <br>
        <p><button @click="getAllDirectories()">获取全部目录</button></p>
        <hr>
        <br>
        <span style="color:red;">所有文件信息获取-ajax提交</span>
        <br>
        <p><button @click="getAllFiles()">获取全部文件</button></p>
        <hr>
        <br>        
        <span style="color:red;">文件授权系统测试-文件上传-ajax提交</span>
        <br>
        <p>选择文件：<input type="file" id="inputFile" @change="uploading($event)"></p>
        <p>目录: <input type="text" name="directory" v-model="directory"/></p>
        <p><button @click="submit($event)">上传</button></p>
        <hr>
        <br>
        <span style="color:red;">文件授权系统测试-文件下载-表单下载</span>
        <form id="downloadFileForm" action="" method="post"  target="_top" class="form form-horizontal">  
          <p>输入文件名: <input type="text" class="input-text"   id="d_filename" style="width:250px"  required="required" v-model="d_filename"/> </p>
          <p>输入目录名：<input type="text" class="input-text"   id="d_directory" style="width:250px"  required="required" v-model="d_directory"/> </p>
          <!-- 下面的button是用于根据filename来更新这个 -->
          <br>
          <h4 style="color:red;">请先点击下面的更新下载地址，获取token类似的地址</h4>
          <input class="btn btn-primary radius" type="submit" id="submit" value="  下载  ">  
        </form>
        <button v-on:click.stop="getDownloadFileURL" style="color:red;">更新下载地址</button>
      </form>
      <hr>
      <br>
      <br>
      <span style="color:red;">文件授权系统测试-文件删除-ajax提交</span>
      <br>
      <p>输入文件名: <input type="text" class="input-text"   name="filename" style="width:250px"  required="required" v-model="delete_filename"/> </p>
      <p>输入目录名：<input type="text" class="input-text"   name="directory" style="width:250px"  required="required" v-model="delete_directory"/> </p>
      <p><button @click="deleteFile()">删除</button></p>
      <hr>
      <br>
      <span style="color:red;">文件授权系统测试-文件重命名-ajax提交</span>
      <br>
      <p>输入文件名: <input type="text" class="input-text"   name="filename" style="width:250px"  required="required" v-model="rename_filename"/> </p>
      <p>输入目录名：<input type="text" class="input-text"   name="directory" style="width:250px"  required="required" v-model="rename_directory"/> </p>
      <p>输入新文件名: <input type="text" class="input-text"   name="new_filename" style="width:250px"  required="required" v-model="rename_new_filename"/> </p>
      <p><button @click="renameFile()">上传</button></p>
      <hr>
      <br>
      <span style="color:red;">文件授权系统测试-新建文件夹-ajax提交</span>
      <br>
      <p>输入目录名：<input type="text" class="input-text"   name="directory" style="width:250px"  required="required" v-model="create_directory"/> </p>
      <p><button @click="createDirectory()">新建文件夹</button></p>
      <hr>
      <br>

      </div>

    </div>
</template>

<script>
import {testHelloApi, getUploadToken, uploadFile,getdownloadURLwithToken, getDeleteFileToken, getRenameFileToken, deleteFile, renameFile, createDirectory, getAllDirectories, getAllFiles  } from '../api/api';

export default {
  name: 'Multifile',
  data () {
    return {
      msg: 'Welcome to Your Vue.js App',
      file: '',
      directory: '',
      d_filename: '',
      d_directory: '',
      delete_filename: '',
      delete_directory: '',
      rename_directory: '',
      rename_filename: '',
      rename_new_filename: '',
      create_directory: '',
    }
  },
  mounted (){
    // testHelloApi({key: "123"})
    //   .then(res => {
    //     console.log(res);
    //   }).catch(err => {
    //     console.log(err);
    //   })
  },
  methods: {
    getAllDirectories() {
      getAllDirectories()
        .then(res => {
          alert("获取全部目录成功，请查看控制台");
          console.log(res.data);
        }).catch(err => {
          console.log(err);
        })
    },
    getAllFiles() {
      getAllFiles()
        .then(res => {
          alert("获取全部文件成功，请查看控制台");
          console.log(res.data);
        }).catch(err => {
          console.log(err);
        })
    },
    deleteFile() {
      let params = new URLSearchParams();
      params.append("fileName", this.delete_filename);
      params.append("directory", this.delete_directory);
      getDeleteFileToken(params)
        .then(res => {
          if(res.data.status == "fail") {
            alert(res.data.message);
          } else {
            console.log(res)
            let params = new URLSearchParams();
            params.append("deleteFileToken", res.data.deleteFileToken); //token需要包含业务数据
            params.append("stsConsumerId", res.data.stsConsumerId);
            deleteFile(params)
              .then(res => {
                alert(res.data.message)
                console.log(res)
              }).catch(err => {
                console.log(err);
              })            
            }
        })
    },
    renameFile() {
      let params = new URLSearchParams();
      params.append("oldFileName", this.rename_filename);
      params.append("directory", this.rename_directory);
      params.append("newFileName", this.rename_new_filename);
      getRenameFileToken(params)
        .then(res => {
          console.log(res)
          let params = new URLSearchParams();
          console.log(res.data.renameFileToken);
          params.append("renameFileToken", res.data.renameFileToken); //token需要包含业务数据
          params.append("stsConsumerId", res.data.stsConsumerId);
          renameFile(params)
            .then(res => {
              alert(res.data.message)
              console.log(res)
            }).catch(err => {
              console.log(err);
            })
        })
    },
    createDirectory() {
      let params = new URLSearchParams();
      params.append("directory", this.create_directory);
      createDirectory(params)
        .then(res => {
          alert(res.data.message);
          console.log(res);
        }).catch(err => {
          console.log(err);
        })
    },
    uploading(event) {
      console.log("uploading")
      this.file = event.target.files[0];
      // let params = {
      //   key: this.file.name
      // };
      // // getUploadToken(params)
      // //   .then(res => {
      // //     console.log(res);
      // //   }).catch(err => {
      // //     console.log(err);
      // //   })
      // console.log(this.file);
    },
    submit(event) {
      //先进行获取token
      console.log("测试获取token");
      console.log(this.file);
      let params = new URLSearchParams();
      params.append("fileName", this.file.name);
      params.append("directory", this.directory);
      getUploadToken(params)
        .then(res => {
          if(res.data.status == 'fail') {
            alert(res.data.message);
            } else {
            console.log(res.data.uploadToken);
            let formdata = new FormData();
            formdata.append('file', this.file);
            formdata.append('uploadToken', res.data.uploadToken);
            formdata.append('stsConsumerId', res.data.stsConsumerId);
            uploadFile(formdata)
              .then(res => {
                console.log(res);
              }).catch(err => {
                console.log(err);
              })
            }
        }).catch(err => {
          console.log(err);
        })

      // event.preventDefault();
      // let formdata = new FormData();
      // formdata.append('file', this.file);
      // formdata.append('directory', this.directory);
      // console.log(formdata);
      // uploadFile(formdata)
      //   .then(res => {
      //     console.log(res);
      //   }).catch(err => {
      //     console.log(err);
      //   })
    },
    getDownloadFileURL() {

      // let downloadurl = "http://localhost:8899/downloadFile?directory=" + this.d_directory + "&fileName=" + this.d_filename;
      let params = new URLSearchParams();
      params.append("fileName", this.d_filename);
      params.append("directory", this.d_directory);
      getdownloadURLwithToken(params)
        .then(res => {
          console.log(res);
          if(res.data.status == "fail") {
            alert(res.data.message);
          } else {
            alert(res.data.message)
            document.getElementById("downloadFileForm").setAttribute("action", res.data.downloadurlwithtoken);
          }
        }).catch(err => {
          console.log(err);
        })
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1, h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>