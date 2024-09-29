document.addEventListener('deviceready', function () {
  const creds = {
    baseURL: 'https://www.valifystage.com/', // Replace with your actual base URL
    bundleKey: 'ad44eb94ca6747beaf99eef02407221f', // Replace with your actual bundle key
    userName: 'mobileusername', // Replace with actual credentials
    password: 'q5YT54wuJ2#mbanR',
    clientID: 'aKM21T4hXpgHFsgNJNTKFpaq4fFpoQvuBsNWuZoQ',
    clientSecret: 'r0tLrtxTue8c4kNmPVgaAFNGSeCWvL4oOZfBnVXoQe2Ffp5rscXXAAhX50BaZEll8ZRtr2BlgD3Nk6QLOPGtjbGXYoCBL9Fn7QCu5CsMlRKDbtwSnUAfKEG30cIv8tdW',
  };

  // Function to fetch token
  const getToken = async () => {
    const url = `${creds.baseURL}/api/o/token/`;
    const body = `username=${creds.userName}&password=${creds.password}&client_id=${creds.clientID}&client_secret=${creds.clientSecret}&grant_type=password`;

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: body,
      });

      if (response.ok) {
        const jsonResponse = await response.json();
        return jsonResponse.access_token;
      } else {
        throw new Error('Failed to retrieve token');
      }
    } catch (error) {
      console.error(error);
      alert('Error: Could not generate token');
      return null;
    }
  };

  const startValify = async () => {
    const token = await getToken();

    if (token) {
      // OCR parameters
      const ocrParams = {
        access_token: token,
        base_url: creds.baseURL,
        bundle_key: creds.bundleKey,
        language: 'en',
        review_data: true
      };

     window.VIDVOCRPlugin.startOCR(ocrParams, function (ocrResponse) {
        console.log('OCR Result:', ocrResponse);
        const parsedResponse = typeof ocrResponse === 'string' ? JSON.parse(ocrResponse) : ocrResponse;

        if (parsedResponse.nameValuePairs?.state === 'SUCCESS') {
          const transactionIdFront = parsedResponse.nameValuePairs?.ocrResult?.ocrResult?.transactionIdFront;
          if (transactionIdFront) {
            setTimeout(() => {
              const livenessParams = {
                access_token: token,
                base_url: creds.baseURL,
                bundle_key: creds.bundleKey,
                language: 'en',
                facematch_ocr_transactionId: transactionIdFront
              };

              window.VIDVLivenessPlugin.startLiveness(livenessParams, function (livenessResponse) {
                console.log('Liveness Result:', livenessResponse);
                alert('Liveness completed successfully');
              }, function (livenessError) {
                console.error('Liveness Error:', livenessError);
                alert('Error: Liveness failed');
              });
            }, 2000); // Delay for 2 seconds
          } else {
            alert('Transaction ID not found in OCR response');
          }
        } else {
          console.log('Current OCR state is not SUCCESS');
        }
      }, function (ocrError) {
        console.error('OCR Error:', ocrError);
        alert('Error: OCR failed');
      });
    }
  };

  const startOCR = async () => {
    const token = await getToken();

    if (token) {
      const ocrParams = {
        access_token: token,
        base_url: creds.baseURL,
        bundle_key: creds.bundleKey,
        language: 'en',
        review_data: true
      };

      window.VIDVOCRPlugin.startOCR(ocrParams, function (ocrResponse) {
        console.log('OCR Result:', ocrResponse);
        alert('OCR completed successfully');
      }, function (ocrError) {
        console.error('OCR Error:', ocrError);
        alert('Error: OCR failed');
      });
    }
  };

  const startLiveness = async () => {
    const token = await getToken();

    if (token) {
      const livenessParams = {
        access_token: token,
        base_url: creds.baseURL,
        bundle_key: creds.bundleKey,
        language: 'en'
      };

      window.VIDVLivenessPlugin.startLiveness(livenessParams, function (livenessResponse) {
        console.log('Liveness Result:', livenessResponse);
        alert('Liveness completed successfully');
      }, function (livenessError) {
        console.error('Liveness Error:', livenessError);
        alert('Error: Liveness failed');
      });
    }
  };

  // Button handlers
  document.getElementById('startValifyButton').addEventListener('click', startValify);
  document.getElementById('startOCRButton').addEventListener('click', startOCR);
  document.getElementById('startLivenessButton').addEventListener('click', startLiveness);
});
