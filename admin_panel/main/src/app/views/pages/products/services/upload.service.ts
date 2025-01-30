import { Injectable } from '@angular/core';
import { storage, ID } from 'src/lib/appwrite';
import { from, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import imageCompression from 'browser-image-compression';

@Injectable({
  providedIn: 'root'
})
export class UploadService {
  private bucketId = '67938113002c3f5c2850';

  private compressionOptions = {
    maxSizeMB: 2,              // Max file size after compression
    maxWidthOrHeight: 1920,    // Maintain good quality for modern displays
    useWebWorker: true,        // Better performance
    initialQuality: 0.8,       // 80% initial quality
    alwaysKeepResolution: true // Maintain aspect ratio
  };

  async compressImage(file: File): Promise<File> {
    try {
      const compressedFile = await imageCompression(file, this.compressionOptions);
      
      // Create a new file with original name but compressed data
      return new File([compressedFile], file.name, {
        type: compressedFile.type
      });
    } catch (error) {
      console.error('Compression failed:', error);
      return file; // Return original file if compression fails
    }
  }

  async uploadFile(file: File): Promise<string | undefined> {
    // Compress the image before uploading
    const compressedFile = await this.compressImage(file);
    
    // Log compression results
    console.log('Original size:', (file.size / 1024 / 1024).toFixed(2) + 'MB');
    console.log('Compressed size:', (compressedFile.size / 1024 / 1024).toFixed(2) + 'MB');

    return from(storage.createFile(
      this.bucketId,
      ID.unique(),
      compressedFile
    )).pipe(
      map(response => {
        return `https://cloud.appwrite.io/v1/storage/buckets/${this.bucketId}/files/${response.$id}/view?project=679380640001125e43cb`;
      })
    ).toPromise();
  }

  deleteFile(fileId: string): Observable<any> {
    return from(storage.deleteFile(this.bucketId, fileId));
  }
}