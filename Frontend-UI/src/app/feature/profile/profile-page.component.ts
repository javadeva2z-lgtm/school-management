import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';

import { ProfileService } from './profile.service';
import { displayNameForClass, SPECIAL_CLASS_NAMES } from '../../common/model/models';

@Component({
  selector: 'app-profile-page',
  imports: [RouterLink],
  templateUrl: './profile-page.component.html'
})
export class ProfilePageComponent {
  protected readonly profile = toSignal(inject(ProfileService).getProfile(), {
    initialValue: {
      id: 0,
      name: '',
      username: '',
      className: '',
      email: '',
      mobile: 'Not available',
      role: '',
      active: true,
      photoUrl: null,
      rollNumber: null,
      classId: null,
      sectionName: '',
      isEWS: null
    }
  });


  displayClassName(classId: number | string): string {

    return displayNameForClass(classId);

  }
}
