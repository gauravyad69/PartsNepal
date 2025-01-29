import { Client, Account, Storage } from 'appwrite';

export const client = new Client();

client
    .setEndpoint('https://cloud.appwrite.io/v1')
    .setProject('679380640001125e43cb'); // Replace with your project ID

export const account = new Account(client);
export const storage = new Storage(client);
export { ID } from 'appwrite';
