import React from 'react';
import DocPaginator from '@theme-original/DocPaginator';
import NewsletterDoc from '@site/src/components/NewsletterDoc';
import Newsletter from '@site/src/components/Newsletter';

export default function DocPaginatorWrapper(props) {
  return (
    <>
      {/*<DocPaginator {...props} />*/}
      <NewsletterDoc/>
    </>
  );
}
