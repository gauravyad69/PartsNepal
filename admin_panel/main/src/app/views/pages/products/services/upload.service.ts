import { Injectable } from '@angular/core';
import { storage, ID } from 'src/lib/appwrite';
import { from, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class UploadService {
  private bucketId = '67938113002c3f5c2850'; // Replace with your bucket ID

  uploadFile(file: File): Observable<string> {
    return from(storage.createFile(
      this.bucketId,
      ID.unique(),
      file
    )).pipe(
      map(response => {
        // Construct the file URL
        return `https://cloud.appwrite.io/v1/storage/buckets/${this.bucketId}/files/${response.$id}/view?project=679380640001125e43cb`;
      })
    );
  }

  deleteFile(fileId: string): Observable<any> {
    return from(storage.deleteFile(this.bucketId, fileId));
  }
} 