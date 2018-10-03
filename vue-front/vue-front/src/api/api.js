import axios from 'axios';

let url = 'http://localhost:8086';
let bussinessServiceURL = 'http://localhost:8900';
let fileStorageServiceURL = 'http://localhost:8899';
let activitServiceURL = 'http://localhost:8762';

export const testStartProcess = (variables, processModelKey) => {
    return axios.post(`${activitServiceURL}/startProcess/${processModelKey}`, variables)
}

export const testPostApi = params => {
    return axios.post(`${url}/hello/123`, params);
};
export const testHelloApi = params => {
    return axios.post(`${bussinessServiceURL}/hello123`, params);
};


export const testGetApi = params => {
    return axios.get(`${url}/hello/123`, params);
}

export const testGetProject = params => {
    return axios.get(`${url}/getProject/2`, params);
}

export const testPostProject = params => {
    return axios.post(`${url}/postProject`, params);
}

export const testAddProject = params => {
    return axios.post(`${url}/addProject`, params);
}

export const testUpdateProject = params => {
    return axios.put(`${url}/updateProject`, params);
}


export const getUploadToken = params => {
    return axios.post(`${bussinessServiceURL}/getUploadToken`, params);
}

export const getUploadToken1 = () => {
    return axios.put(`${bussinessServiceURL}/getUploadToken`);
}

export const getdownloadURLwithToken = params => {
    return axios.post(`${bussinessServiceURL}/generateDownloadURLWithToken`, params);
}

export const uploadFile = (params) => {
    return axios({
        method: 'post',
        url: `${fileStorageServiceURL}/uploadFile`,
        data: params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

export const getDeleteFileToken = params => {
    return axios.post(`${bussinessServiceURL}/getDeleteFileToken`, params);
}

export const getRenameFileToken = params => {
    return axios.post(`${bussinessServiceURL}/getRenameFileToken`, params);
}

export const createDirectory = params => {
    return axios.post(`${bussinessServiceURL}/createDirectory`, params);
}

export const deleteFile = params => {
    return  axios.post(`${fileStorageServiceURL}/deleteFile`, params);
}

export const renameFile = params => {
    return axios.post(`${fileStorageServiceURL}/renameFile`, params);
}

export const getAllDirectories = () => {
    return axios.post(`${bussinessServiceURL}/getAllDirectories`);
}

export const getAllFiles = () => {
    return axios.post(`${bussinessServiceURL}/getAllFiles`);
}