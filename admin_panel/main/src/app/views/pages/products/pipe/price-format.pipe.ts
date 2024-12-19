import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'priceFormat',
  standalone: true
})
export class PriceFormatPipe implements PipeTransform {
  transform(value: number | undefined): number {
    return value ? value / 100 : 0;  // Return 0 if value is undefined
  }
}