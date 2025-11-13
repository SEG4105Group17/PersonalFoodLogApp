# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`

from firebase_functions import https_fn
from firebase_functions.options import set_global_options
from firebase_admin import initialize_app, storage
import random
import cv2
import numpy as np

# For cost control, you can set the maximum number of containers that can be
# running at the same time. This helps mitigate the impact of unexpected
# traffic spikes by instead downgrading performance. This limit is a per-function
# limit. You can override the limit for each function using the max_instances
# parameter in the decorator, e.g. @https_fn.on_request(max_instances=5).
set_global_options(max_instances=10)

initialize_app()


@https_fn.on_call()
def random_num(req: https_fn.CallableRequest):
    value = random.randint(0,100)

    return {"randomValue": value}


@https_fn.on_call()
def process_image(req: https_fn.CallableRequest):
    # Get image path
    inputData = req.data
    storagePath = inputData.get("storagePath")

    if not storagePath:
        raise https_fn.HttpsError(
            code="invalid-argument",
            message="No storage path provided"
        )

    # Get item from storage
    bucket = storage.bucket()
    imageBlob = bucket.blob(storagePath)

    # Download image
    imageBytes = imageBlob.download_as_bytes()

    # TO BE IMPLEMENTED: Run AI Model on the image
    # For the proof of concept, we will instead base the result on the first pixel
    # We will return the RGB values of the first pixel [R, G, B]
    numpyArray = np.frombuffer(imageBytes, np.uint8)
    img = cv2.imdecode(numpyArray, cv2.IMREAD_COLOR)
    b, g, r = img[0,0]

    return {"result": [int(r), int(g), int(b)]}