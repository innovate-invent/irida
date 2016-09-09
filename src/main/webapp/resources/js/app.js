/* global TL */

const angular = require('angular');
const $ = require('jquery');
const union = require('lodash/union');

let deps = union(window.dependencies || [], [
  'ngAria',
  'ngAnimate',
  'ui.bootstrap',
  'irida.session',
  'irida.notifications',
  'irida.cart'
]);

const app = angular.module('irida', deps)
  .config($httpProvider => {
    $httpProvider.defaults.headers
      .post['Content-Type'] = 'application/x-www-form-urlencoded';

    // Make sure that all ajax form data is sent in the correct format.
    $httpProvider.defaults.transformRequest = data => {
      if (typeof data === 'undefined') {
        return data;
      }
      return $.param(data);
    };
  })
  .run(uibPaginationConfig => {
    uibPaginationConfig.firstText = TL.lang.page.first;
    uibPaginationConfig.previousText = TL.lang.page.prev;
    uibPaginationConfig.nextText = TL.lang.page.next;
    uibPaginationConfig.lastText = TL.lang.page.last;
    uibPaginationConfig.boundaryLinks = true;
    uibPaginationConfig.directionLinks = true;
    uibPaginationConfig.maxSize = 8;
    uibPaginationConfig.rotate = false;
  });

export default app;
