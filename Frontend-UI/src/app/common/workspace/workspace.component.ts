import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { MenuItem, Role } from '../model/models';

@Component({
  selector: 'app-workspace',
  imports: [RouterLink],
  templateUrl: './workspace.component.html'
})
export class WorkspaceComponent {
  @Input({ required: true }) role!: Role;
  @Input({ required: true }) menus!: Record<Role, MenuItem[]>;

  protected routeFor(item: MenuItem, role: Role): string[] {
    if (role === 'Teacher' && item.id === 'announcements') return ['/workspace/announcements'];
    if (role === 'Teacher' && item.id === 'events') return ['/workspace/events'];
    if (role === 'Teacher' && item.id === 'exam-result') return ['/workspace/exam-result'];
    if ((role === 'Admin' || role === 'Manager') && item.id === 'subjects') return ['/workspace/subjects'];
    if ((role === 'Admin' || role === 'Manager') && item.id === 'optional-fee-mapping') return ['/workspace/optional-fee-mapping'];
    if (role === 'Admin' && item.id === 'user-accounts') return ['/workspace/user-accounts'];
    if (role === 'SuperAdmin' && item.id === 'schools') return ['/workspace/schools'];
    if (role === 'Student' && item.id === 'announcements') return ['/student/announcements'];
    if (role === 'Student' && item.id === 'events') return ['/student/events'];
    if (role === 'Student' && item.id === 'result') return ['/student/result'];
    return role === 'Student' ? ['/student', item.id] : ['/workspace', item.id];
  }
}
