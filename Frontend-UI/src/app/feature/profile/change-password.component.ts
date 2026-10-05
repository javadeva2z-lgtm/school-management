import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { PasswordManagedUser } from '../../common/model/models';
import { AuthService } from '../../core/auth/auth.service';
import { AuthSessionService } from '../../core/auth/auth-session.service';

@Component({
  selector: 'app-change-password',
  imports: [FormsModule, RouterLink],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css'
})
export class ChangePasswordComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly authSession = inject(AuthSessionService);

  protected readonly isAdmin = this.authSession.role === 'Admin';
  protected readonly currentPassword = signal('');
  protected readonly newPassword = signal('');
  protected readonly confirmPassword = signal('');
  protected readonly selectedUsername = signal('');
  protected readonly adminNewPassword = signal('');
  protected readonly adminConfirmPassword = signal('');
  protected readonly managedUsers = signal<PasswordManagedUser[]>([]);
  protected readonly isLoadingUsers = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');

  ngOnInit(): void {
    if (this.isAdmin) {
      this.loadManagedUsers();
    }
  }

  protected changeOwnPassword(): void {
    const currentPassword = this.currentPassword();
    const newPassword = this.newPassword();
    if (!currentPassword || newPassword.length < 6 || newPassword !== this.confirmPassword()) {
      this.setError('Enter your current password, a new password of at least 6 characters, and matching confirmation.');
      return;
    }

    this.isSaving.set(true);
    this.clearMessages();
    this.authService.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.currentPassword.set('');
        this.newPassword.set('');
        this.confirmPassword.set('');
        this.successMessage.set('Your password has been changed.');
        this.isSaving.set(false);
      },
      error: () => {
        this.setError('Unable to change your password. Check your current password and try again.');
        this.isSaving.set(false);
      }
    });
  }

  protected resetUserPassword(): void {
    const username = this.selectedUsername();
    const newPassword = this.adminNewPassword();
    if (!username || newPassword.length < 6 || newPassword !== this.adminConfirmPassword()) {
      this.setError('Select a user and enter a new password of at least 6 characters with matching confirmation.');
      return;
    }

    this.isSaving.set(true);
    this.clearMessages();
    this.authService.adminResetPassword(username, newPassword).subscribe({
      next: () => {
        this.adminNewPassword.set('');
        this.adminConfirmPassword.set('');
        this.successMessage.set(`Password reset successfully for ${username}.`);
        this.isSaving.set(false);
      },
      error: () => {
        this.setError(`Unable to reset the password for ${username}. Please try again.`);
        this.isSaving.set(false);
      }
    });
  }

  private loadManagedUsers(): void {
    this.isLoadingUsers.set(true);
    this.authService.getPasswordManagedUsers().subscribe({
      next: users => {
        this.managedUsers.set(users);
        this.isLoadingUsers.set(false);
      },
      error: () => {
        this.setError('Unable to load Student, Teacher, and Manager accounts.');
        this.isLoadingUsers.set(false);
      }
    });
  }

  private setError(message: string): void {
    this.errorMessage.set(message);
    this.successMessage.set('');
  }

  private clearMessages(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
  }
}
