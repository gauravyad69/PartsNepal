import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { map, Observable, tap } from 'rxjs';
import { environment } from 'src/environments/environment';
import { LocalstorageService } from './localstorage.service';
import { AuthResponse, LoginRequest, RegisterRequest, AccountType } from '../../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  refreshTokenTimeout: any;

  constructor(
    private http: HttpClient,
    private _token: LocalstorageService,
    private router: Router
  ) { }

  login(identifier: string, password: string, isPhoneLogin: boolean): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.api}/auth/login`, { identifier, password, isPhoneLogin }).pipe(
      tap(response => {
        if (response.token) {
          this._token.setToken(response.token);
          this.startRefreshTokenTimer();
        }
      })
    );
  }

  register(name: string, email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.api}/users/`, { name, email, password, accountType: AccountType.PERSONAL });
  }

  loggedIn(): boolean {
    const token = this._token.getToken();
    if (!token) return false;

    try {
      const tokenDecode = JSON.parse(atob(token.split('.')[1]));
      return !this._tokenExpired(tokenDecode.exp);
    } catch {
      return false;
    }
  }

  private _tokenExpired(expiration: number): boolean {
    if (!expiration) return true;
    return Math.floor(new Date().getTime() / 1000) >= expiration;
  }

  logout() {
    this._token.removeToken();
    this.stopRefreshTokenTimer();
    this.router.navigate(['/auth/login']);
  }

  refreshToken(): Observable<any> {
    const token = this._token.getToken();
    return this.http.post<any>(`${environment.api}/auth/refresh-token`, { token }).pipe(
      map((response: any) => {

        this._token.setToken(response.access_token);

        const expiration = response.refresh_token;
        localStorage.setItem('expiration', expiration);

        this.startRefreshTokenTimer();
        return true;
      })
    );
  }

  startRefreshTokenTimer() {
    console.log('start Refresh Token Timer');
    const jwtToken = this._token.getToken();
    const jwtTokenDecode = JSON.parse(atob(jwtToken.split('.')[1]));
    const expires = new Date(jwtTokenDecode.exp * 1000);
    const timeout = expires.getTime() - Date.now() - (60 * 1000);
    console.log('timeout', timeout);   
    this.refreshTokenTimeout = setTimeout(() => this.refreshToken().subscribe(), timeout);
  }

  stopRefreshTokenTimer() {
    clearTimeout(this.refreshTokenTimeout);
  }
}
