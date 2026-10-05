import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { UserRegistrationRequest } from '../../common/model/models';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-admin-user-accounts',
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-user-accounts.component.html',
  styleUrl: './admin-user-accounts.component.css'
})
export class AdminUserAccountsComponent {
  private readonly authService = inject(AuthService);

  protected readonly activeTab = signal<'new' | 'existing'>('new');
  protected readonly managers = signal<{ username: string; phoneNumber: string | null }[]>([]);
  protected readonly isLoadingManagers = signal(false);
  protected readonly deletingManager = signal('');
  protected readonly username = signal('');
  protected readonly password = signal('');
  protected readonly phoneNumber = signal('');
  protected readonly role = signal<'ADMIN' | 'MANAGER'>('MANAGER');
  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');

  protected selectTab(tab: 'new' | 'existing'): void {
    this.activeTab.set(tab);
    this.errorMessage.set('');
    this.successMessage.set('');
    if (tab === 'existing') {
      this.loadManagers();
    }
  }

  protected submit(): void {
    const username = this.username().trim();
    const phoneNumber = this.phoneNumber().trim();
    const password = this.password();
    if (!username || !phoneNumber || password.length < 6 || this.isSaving()) {
      this.errorMessage.set('Enter a username, phone number, and a password of at least 6 characters.');
      this.successMessage.set('');
      return;
    }

    const request: UserRegistrationRequest = {
      username,
      password,
      phoneNumber,
      role: this.role()
    };
    this.isSaving.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');
    this.authService.createAdminOrManager(request).subscribe({
      next: () => {
        this.username.set('');
        this.password.set('');
        this.phoneNumber.set('');
        this.role.set('MANAGER');
        this.successMessage.set(`${request.role === 'ADMIN' ? 'Admin' : 'Manager'} account created successfully.`);
        this.isSaving.set(false);
        if (request.role === 'MANAGER' && this.activeTab() === 'existing') {
          this.loadManagers();
        }
      },
      error: () => {
        this.errorMessage.set('Unable to create the account. The username may already exist; check the details and try again.');
        this.isSaving.set(false);
      }
    });
  }

  protected deleteManager(username: string): void {
    if (this.deletingManager() || !window.confirm(`Delete Manager account "${username}"? This cannot be undone.`)) {
      return;
    }

    this.deletingManager.set(username);
    this.errorMessage.set('');
    this.successMessage.set('');
    this.authService.deleteManager(username).subscribe({
      next: () => {
        this.managers.update(managers => managers.filter(manager => manager.username !== username));
        this.successMessage.set(`Manager account "${username}" deleted successfully.`);
        this.deletingManager.set('');
      },
      error: () => {
        this.errorMessage.set(`Unable to delete Manager account "${username}". Please try again.`);
        this.deletingManager.set('');
      }
    });
  }

  private loadManagers(): void {
    this.isLoadingManagers.set(true);
    this.authService.getManagers().subscribe({
      next: managers => {
        this.managers.set(managers);
        this.isLoadingManagers.set(false);
      },
      error: () => {
        this.errorMessage.set('Unable to load Manager accounts. Please refresh the page and try again.');
        this.isLoadingManagers.set(false);
      }
    });
  }
}
