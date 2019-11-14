import {environment} from '../../environments/environment';

export class BaseService {

  protected baseurl: string;

  constructor() {
    this.baseurl = environment.baseUrl;
  }
}
